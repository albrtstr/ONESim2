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
public class DegreeCentrality_BubbleRap implements RoutingDecisionEngine, CentralityDetectionEngine_intf {

    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    protected Centrality centrality;

    public DegreeCentrality_BubbleRap(Settings s) {
        if (s.contains(CENTRALITY_ALG_SETTING)) {
            this.centrality = (Centrality) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
        } else {
            this.centrality = new SWindowCentrality(s);
        }
    }

    public DegreeCentrality_BubbleRap(DegreeCentrality_BubbleRap proto) {
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
        DegreeCentrality_BubbleRap dc = this.getOtherDecisionEngine(peer);
    }

    private DegreeCentrality_BubbleRap getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (DegreeCentrality_BubbleRap) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
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
        DegreeCentrality_BubbleRap dc = getOtherDecisionEngine(otherHost);

        if (dc.getGlobalDegreeCentrality() > this.getGlobalDegreeCentrality()) {
            return true;
        } else {
            return dc.getLocalDegreeCentrality() > this.getLocalDegreeCentrality();
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

    @Override
    public RoutingDecisionEngine replicate() {
        return new DegreeCentrality_BubbleRap(this);
    }

    @Override
    public double getGlobalDegreeCentrality() {
        return this.centrality.getGlobalCentrality(connHistory);
    }

    @Override
    public double getLocalDegreeCentrality() {
        return 0;
    }

    @Override
    public int[] getArrayCentrality() {
       int[] centralities = (int[]) [this.centrality.getGlobalCentrality(connHistory)];
    }

}
