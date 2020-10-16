/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SimClock;
import core.UpdateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import report.Report;

/**
 *
 * @author ASUS
 */
public class CopyPesanReport_16Okt extends Report implements MessageListener, UpdateListener {

    public static final String MESSTATIC_REPORT_INTERVAL = "messageInterval";

    public static final int DEFAULT_MESSTATIC_REPORT_INTERVAL = 5;

    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private int updateCounter = 0;

    private Map<DTNHost, List<Double>> messageCounts;
    private Map<DTNHost, List<Double>> messageInterval;
    //private Map<DTNHost, Double> messageMap;
    private Map<Integer, Integer> msgCopied = new HashMap<Integer, Integer>();

    private Map<String, Double> creationTimes;
    private List<Double> latencies;
    private List<Integer> hopCounts;
    private List<Double> msgBufferTime;
    private List<Double> rtt; // round trip times

    private int nrofDropped;
    private int nrofRemoved;
    private int nrofStarted;
    private int nrofAborted;
    private int nrofRelayed;
    private int nrofCreated;
    private int nrofResponseReqCreated;
    private int nrofResponseDelivered;
    private int nrofDelivered;

    private int nrofCopied;

    public CopyPesanReport_16Okt() {
        super();
        init();

        this.nrofRelayed = 0;
        this.updateCounter = 0;
        Settings settings = getSettings();
        if (settings.contains(MESSTATIC_REPORT_INTERVAL)) {
            interval = settings.getInt(MESSTATIC_REPORT_INTERVAL);
        } else {
            interval = -1;
        }
        if (interval < 0) {
            interval = DEFAULT_MESSTATIC_REPORT_INTERVAL;
        }

    }

    @Override
    protected void init() {
        super.init();
        this.creationTimes = new HashMap<String, Double>();
        this.latencies = new ArrayList<Double>();
        this.msgBufferTime = new ArrayList<Double>();
        this.hopCounts = new ArrayList<Integer>();
        this.rtt = new ArrayList<Double>();

        this.nrofDropped = 0;
        this.nrofRemoved = 0;
        this.nrofStarted = 0;
        this.nrofAborted = 0;
        this.nrofRelayed = 0;
        this.nrofCreated = 0;
        this.nrofResponseReqCreated = 0;
        this.nrofResponseDelivered = 0;
        this.nrofDelivered = 0;
    }

    @Override
    public void newMessage(Message m) {
        if (isWarmup()) {
            addWarmupID(m.getId());
            return;
        }

        this.creationTimes.put(m.getId(), getSimTime());
        this.nrofCreated++;
        if (m.getResponseSize() > 0) {
            this.nrofResponseReqCreated++;
        }
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }

        this.nrofStarted++;
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (isWarmupID(m.getId())) {
            return;
        }
        if (dropped) {
            this.nrofDropped++;
        } else {
            this.nrofRemoved++;
        }

        this.msgBufferTime.add(getSimTime() - m.getReceiveTime());
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }
        this.nrofAborted++;
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        if (isWarmupID(m.getId())) {
            return;
        }

        this.nrofRelayed++;
        if (firstDelivery) {
            this.latencies.add(getSimTime() - this.creationTimes.get(m.getId()));
            this.nrofDelivered++;
            this.hopCounts.add(m.getHops().size() - 1);

            if (m.isResponse()) {
                this.rtt.add(getSimTime() - m.getRequest().getCreationTime());
                this.nrofResponseDelivered++;
            }
        }
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (isWarmup()) {
            return;
        }
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            updateCounter++;
            printLine();
        }
    }

    @Override
    public void done() {
        write("Time \t Epidemic \t Message");
        for (Map.Entry<Integer, Integer> entry : msgCopied.entrySet()) {
            int a = entry.getKey() * interval;
            int b = entry.getValue();
            String print = a + "\t" + b;
            write(print);
        }
        super.done();

//        write("Message stats for scenario " + getScenarioName() + "\nsim_time: " + format(getSimTime()));
//        double deliveryProb = 0;
//        double responseProb = 0;
//        double overHead = Double.NaN;
//
//        if (this.nrofCreated > 0) {
//            deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
//        }
//        if (this.nrofDelivered > 0) {
//            overHead = (1.0 * (this.nrofRelayed - this.nrofDelivered)) / this.nrofDelivered;
//        }
//        if (this.nrofResponseReqCreated > 0) {
//            responseProb = (1.0 * this.nrofResponseDelivered) / this.nrofResponseReqCreated;
//        }
//
//        String statsText = "created: " + this.nrofCreated
//                + "\nstarted: " + this.nrofStarted
//                + "\nrelayed: " + this.nrofRelayed
//                + "\naborted: " + this.nrofAborted
//                + "\ndropped: " + this.nrofDropped
//                + "\nremoved: " + this.nrofRemoved
//                + "\ndelivered: " + this.nrofDelivered
//                + "\ndelivery_prob: " + format(deliveryProb)
//                + "\nresponse_prob: " + format(responseProb)
//                + "\noverhead_ratio: " + format(overHead)
//                + "\nlatency_avg: " + getAverage(this.latencies)
//                + "\nlatency_med: " + getMedian(this.latencies)
//                + "\nhopcount_avg: " + getIntAverage(this.hopCounts)
//                + "\nhopcount_med: " + getIntMedian(this.hopCounts)
//                + "\nbuffertime_avg: " + getAverage(this.msgBufferTime)
//                + "\nbuffertime_med: " + getMedian(this.msgBufferTime)
//                + "\nrtt_avg: " + getAverage(this.rtt)
//                + "\nrtt_med: " + getMedian(this.rtt);
//
//        write(statsText);
//        super.done();
    }

    private void printLine() {
        msgCopied.put(updateCounter, nrofRelayed);
    }

}
