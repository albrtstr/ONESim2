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
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author ASUS
 */
public class DistributedBubbleRap_interContact implements RoutingDecisionEngine, help_average{

    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, Double> startTimeStamp;
    
    double total = 0;
    double jumlah;
    
    
    public DistributedBubbleRap_interContact(Settings s){
        startTimeStamp = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }
    
    public DistributedBubbleRap_interContact(DistributedBubbleRap_interContact proto){
        startTimeStamp = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }
    
    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        //intracontact
        double time;
        if (startTimeStamp.get(peer) == null) {
            time = 0;
        } else {
            time = startTimeStamp.get(peer);
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
        startTimeStamp.remove(peer);
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        DistributedBubbleRap_interContact de = this.getOtherDecisionEngine(peer);
        this.startTimeStamp.put(peer, SimClock.getTime());
        de.startTimeStamp.put(myHost, SimClock.getTime());
    }
    
    private DistributedBubbleRap_interContact getOtherDecisionEngine(DTNHost h){
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This Router only works "
                + "with other routers of same type";
        
        return (DistributedBubbleRap_interContact) ((DecisionEngineRouter)otherRouter).getDecisionEngine();
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
    
    private double getConnHis(DTNHost host){
        List<Duration> contact = new LinkedList();
        if (connHistory.containsKey(host)) {
            contact = connHistory.get(host);
        }
        for (Iterator<Duration> iterator = contact.iterator(); iterator.hasNext();) {
            Duration next = iterator.next();
            total = total + (next.end - next.start);
        }
        jumlah = contact.size();
        return total / jumlah;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost other = m.getTo();
        DistributedBubbleRap_interContact de = getOtherDecisionEngine(otherHost);
        
        double peer = de.getConnHis(other);
        double me = this.getConnHis(other);
        if (me < peer) {
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

    @Override
    public RoutingDecisionEngine replicate() {
        return new DistributedBubbleRap_interContact(this);
    }

    @Override
    public double rataRata() {
        double Total = 0;
        double average = 0;
        double jumlah = 0;
        for (Map.Entry<DTNHost, List<Duration>> entry: connHistory.entrySet()) {
            DTNHost key = entry.getKey();
            Total = Total + getConnHis(key);
        }
        
        jumlah = connHistory.size();
        average = Total / jumlah;
        
        return average;
    }
    
    
}
