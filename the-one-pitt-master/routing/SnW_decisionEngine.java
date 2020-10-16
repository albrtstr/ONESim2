/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import java.util.HashMap;
import java.util.Map;
import routing.decisionengine.SnFDecisionEngine;

/**
 *
 * @author ASUS
 */
public class SnW_decisionEngine implements RoutingDecisionEngine {

    /**
     * identifier for the initial number of copies setting ({@value})
     */
    public static final String NROF_COPIES_S = "nrofCopies";
    /**
     * Message property key for the remaining available copies of a message
     */
    public static final String MSG_COUNT_PROPERTY = "SprayAndFocus.copies";
    /**
     * identifier for the difference in timer values needed to forward on a
     * message copy
     */
    public static final String TIMER_THRESHOLD_S = "transitivityTimerThreshold";

    protected static final double DEFAULT_TIMEDIFF = 300;
    protected static final double defaultTransitivityThreshold = 1.0;

    protected int initialNrofCopies;
    protected boolean isBinary;
    protected double transitivityTimerThreshold;

    /**
     * Stores information about nodes with which this host has come in contact
     */
    protected Map<DTNHost, Double> recentEncounters;

    public SnW_decisionEngine(Settings s) {
        initialNrofCopies = s.getInt(NROF_COPIES_S);

        if (s.contains(TIMER_THRESHOLD_S)) {
            transitivityTimerThreshold = s.getDouble(TIMER_THRESHOLD_S);
        } else {
            transitivityTimerThreshold = defaultTransitivityThreshold;
        }

        recentEncounters = new HashMap<DTNHost, Double>();
    }

    public SnW_decisionEngine(SnW_decisionEngine snf) {
        this.initialNrofCopies = snf.initialNrofCopies;
        this.transitivityTimerThreshold = snf.transitivityTimerThreshold;
        recentEncounters = new HashMap<DTNHost, Double>();
    }

    public RoutingDecisionEngine replicate() {
        return new SnW_decisionEngine(this);
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {

    }

    @Override
    public boolean newMessage(Message m) {
        m.addProperty(MSG_COUNT_PROPERTY, initialNrofCopies);
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (isBinary) {
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            nrofCopies = 1;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        int nrofCopies = (int) m.getProperty(MSG_COUNT_PROPERTY);
        return nrofCopies > 1;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        int nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (isBinary) {
            nrofCopies /= 2;
        } else {
            nrofCopies--;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

}
