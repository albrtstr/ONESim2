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
import java.util.Iterator;
import java.util.LinkedList;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author ASUS
 */
public class DistributedBubbleRap_connHistory implements RoutingDecisionEngine {

    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, Double> startTimestamps;

    double total = 0;
    double jumlah;

    public DistributedBubbleRap_connHistory(Settings s) {
        this.startTimestamps = startTimestamps;
        this.connHistory = connHistory;
    }

    public DistributedBubbleRap_connHistory(DistributedBubbleRap_connHistory proto) {
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {

    }

    private double getConnectionHistory(DTNHost host) {
        List<Duration> time = new LinkedList();
        if (connHistory.containsKey(host)) {
            time = connHistory.get(host);

        }

        for (Iterator<Duration> iterator = time.iterator(); iterator.hasNext();) {
            Duration next = iterator.next();
            total = total + (next.end - next.start);
        }
        jumlah = time.size();
        return total / jumlah;
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {

        double time;

        if (startTimestamps.get(peer) == null) {
            time = 0;
        } else {
            time = startTimestamps.get(peer);
        }

        double endtime = SimClock.getIntTime();

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

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        DistributedBubbleRap_connHistory de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());
    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() != aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

//    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
//
//    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    private DistributedBubbleRap_connHistory getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + "with other routers of same type";

        return (DistributedBubbleRap_connHistory) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new DistributedBubbleRap_connHistory(this);
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true;
        }

        DTNHost hostOther = m.getTo();
        DistributedBubbleRap_connHistory dbr = getOtherDecisionEngine(otherHost);

        Double peer = dbr.getConnectionHistory(hostOther);
        Double temp = this.getConnectionHistory(hostOther);
        if (temp > peer) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void update(DTNHost thisHost) {

    }

}
