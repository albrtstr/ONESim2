/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.community;

import core.Connection;
import core.DTNHost;
import core.Message;
import java.util.List;
import java.util.Map;
import java.util.Set;
import core.*;
import java.util.HashMap;
import java.util.LinkedList;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author ASUS
 */
public class DistributedBubbleRap_connHistory implements RoutingDecisionEngine, CommunityDetectionEngine {

    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";

    protected Map<DTNHost, List<Duration>> connHistory;
    private Map<DTNHost, Double> startTimestamps;

    private CommunityDetection community;
    private Centrality centrality;

    public DistributedBubbleRap_connHistory(Map<DTNHost, List<Duration>> connHistory) {
        this.connHistory = connHistory;
    }

    public DistributedBubbleRap_connHistory(Settings s) {
//        if (s.contains(COMMUNITY_ALG_SETTING)) {
//            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
//        } else {
//            this.community = new SimpleCommunityDetection(s);
//        }
//
//        if (s.contains(CENTRALITY_ALG_SETTING)) {
//            this.centrality = (Centrality) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
//        } else {
//            this.centrality = new SWindowCentrality(s);
//        }
    }
    
    public DistributedBubbleRap_connHistory(DistributedBubbleRap_connHistory proto){
        this.community = proto.community.replicate();
        this.centrality = proto.centrality.replicate();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double starttime = startTimestamps.get(peer);
        double endtime = SimClock.getTime();
        
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        }
        else {
            history = connHistory.get(peer);
        }
        
        if (endtime - starttime > 0) {
            history.add(new Duration(starttime, endtime));
        }
        CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community;
        
        community.connectionLost(thisHost, peer, peerCD, history);
        startTimestamps.remove(peer);
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        DistributedBubbleRap_connHistory de = this.getOtherDecisionEngine(peer);
        
        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());
        
        this.community.newConnection(myHost, peer, de.community);
    }

    @Override
    public boolean newMessage(Message m) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RoutingDecisionEngine replicate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<DTNHost> getLocalCommunity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private DistributedBubbleRap_connHistory getOtherDecisionEngine(DTNHost h){
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + "with other routers of same type";
        
        return (DistributedBubbleRap_connHistory) ((DecisionEngineRouter)otherRouter).getDecisionEngine();
    }
    
    protected double getLocalCentrality(){
        return this.centrality.getLocalCentrality(connHistory, community);
    }
    
    protected double getGlobalCentrality(){
        return this.centrality.getGlobalCentrality(connHistory);
    }

}
