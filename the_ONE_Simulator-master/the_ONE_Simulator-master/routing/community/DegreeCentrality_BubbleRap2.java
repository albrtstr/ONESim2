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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author ASUS
 */
public class DegreeCentrality_BubbleRap2 implements RoutingDecisionEngine, degreeCentrality_array {

    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    protected Centrality_intf centrality;

    public DegreeCentrality_BubbleRap2(Settings s) {
        if (s.contains(CENTRALITY_ALG_SETTING)) {
            this.centrality = (Centrality_intf) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
        }

    }

    public DegreeCentrality_BubbleRap2(DegreeCentrality_BubbleRap2 proto) {
        this.centrality = proto.centrality.replicate();
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

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        DegreeCentrality_BubbleRap2 dc = this.getOtherDecisionEngine(peer);
    }

    private DegreeCentrality_BubbleRap2 getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (DegreeCentrality_BubbleRap2) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
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
            return true;
        }

        DTNHost dest = m.getTo();
        DegreeCentrality_BubbleRap2 dc = getOtherDecisionEngine(otherHost);

        if (dc.getGlobalCentrality(connHistory) > this.getGlobalCentrality(connHistory)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    @Override
    public void update(DTNHost thisHost) {

    }

    public double getGlobalCentrality(Map<DTNHost, List<Duration>> connHistory) {
        return this.centrality.getGlobalCentrality(connHistory);
    }

    public double getLocalCentrality(Map<DTNHost, List<Duration>> connHistory, CommunityDetection cd) {
        return 0;
    }

//    @Override
//    public DegreeCentrality_BubbleRap2 replicate() {
//        return new DegreeCentrality_BubbleRap2(this);
//    }
    
    @Override
    public RoutingDecisionEngine replicate(){
        return new DegreeCentrality_BubbleRap2(this);
    }

    @Override
    public int[] Centralitiess() {
        return this.centrality.getArrayGlobalCentrality(connHistory);
    }

}
