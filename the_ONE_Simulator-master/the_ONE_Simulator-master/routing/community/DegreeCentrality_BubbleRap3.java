/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.community;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.*;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author ASUS
 */
public class DegreeCentrality_BubbleRap3 implements RoutingDecisionEngine, CommunityDetectionEngine, degreeCentrality_array {

    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    protected CommunityDetection community;
    protected Centrality_intf centrality;

    public DegreeCentrality_BubbleRap3(Settings s) {
        if (s.contains(COMMUNITY_ALG_SETTING)) {
            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
        } else {
            this.community = new SimpleCommunityDetection(s);
        }

        if (s.contains(CENTRALITY_ALG_SETTING)) {
            this.centrality = (Centrality_intf) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
        }
    }

    public DegreeCentrality_BubbleRap3(DegreeCentrality_BubbleRap3 proto) {
        this.community = proto.community.replicate();
        this.centrality = (Centrality_intf) proto.centrality.replicate();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double time = check(thisHost, peer);
        double endtime = SimClock.getTime();

        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }

        if (endtime - time > 0) {
            history.add(new Duration(time, endtime));
        }

        startTimestamps.remove(peer);
    }

    public double check(DTNHost thisHost, DTNHost peer) {
        if (startTimestamps.containsKey(thisHost)) {
            startTimestamps.get(peer);
        }
        return 0;
    }

    private DegreeCentrality_BubbleRap3 getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (DegreeCentrality_BubbleRap3) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        DegreeCentrality_BubbleRap3 dc = this.getOtherDecisionEngine(peer);
    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true; // trivial to deliver to final dest
        }
        /*
		 * Here is where we decide when to forward along a message. 
		 * 
		 * DiBuBB works such that it first forwards to the most globally central
		 * nodes in the network until it finds a node that has the message's 
		 * destination as part of it's local community. At this point, it uses 
		 * the local centrality metric to forward a message within the community. 
         */
        DTNHost dest = m.getTo();
        DegreeCentrality_BubbleRap3 de = getOtherDecisionEngine(otherHost);
        // Which of us has the dest in our local communities, this host or the peer
        boolean peerInCommunity = de.commumesWithHost(dest);
        boolean meInCommunity = this.commumesWithHost(dest);

        if (peerInCommunity && !meInCommunity) // peer is in local commun. of dest
        {
            return true;
        } else if (!peerInCommunity && meInCommunity) // I'm in local commun. of dest
        {
            return false;
        } else if (peerInCommunity) // we're both in the local community of destination
        {
            // Forward to the one with the higher local centrality (in our community)
            if (de.getLocalCentrality() > this.getLocalCentrality()) {
                return true;
            } else {
                return false;
            }
        } // Neither in local community, forward to more globally central node
        else if (de.getGlobalCentrality() > this.getGlobalCentrality()) {
            return true;
        }

        return false;
    }

    protected boolean commumesWithHost(DTNHost h) {
        return community.isHostInCommunity(h);
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        // DiBuBB allows a node to remove a message once it's forwarded it into the
        // local community of the destination
        DegreeCentrality_BubbleRap3 de = this.getOtherDecisionEngine(otherHost);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        DegreeCentrality_BubbleRap3 de = this.getOtherDecisionEngine(hostReportingOld);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }

    @Override
    public void update(DTNHost thisHost) {

    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new DegreeCentrality_BubbleRap3(this);
    }

    @Override
    public Set<DTNHost> getLocalCommunity() {
        return this.community.getLocalCommunity();
    }

    @Override
    public int[] Centralitiess() {
        return this.centrality.getArrayGlobalCentrality(connHistory);
    }

    protected double getLocalCentrality() {
        return this.centrality.getLocalCentrality(connHistory, community);
    }

    protected double getGlobalCentrality() {
        return this.centrality.getGlobalCentrality(connHistory);
    }

}
