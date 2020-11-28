/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.community;

import core.DTNHost;
import core.Settings;
import core.SimClock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static routing.community.AverageWinCentrality1.EPOCH_COUNT;
import static routing.community.CWindowCentrality.CENTRALITY_TIME_WINDOW;
import static routing.community.CWindowCentrality.CENTRALITY_WINDOW_SETTING;
import static routing.community.CWindowCentrality.COMPUTE_INTERVAL;

/**
 *
 * @author ASUS
 */
public class CWindowCentrality_intf implements Centrality_intf {

    public static final String CENTRALITY_WINDOW_SETTING = "timeWindow";
    public static final String COMPUTATION_INTERVAL_SETTING = "computeInterval";
    public static final String EPOCH_COUNT_SETTING = "nrOfEpochsToAvg";
    protected static int COMPUTE_INTERVAL = 600; // seconds, i.e. 10 minutes
    protected static int CENTRALITY_TIME_WINDOW = 21600; // 6 hours
    protected static int EPOCH_COUNT = 787; // CHANGED FROM 5,48;
    protected double globalCentrality;
    protected double localCentrality;
    protected int lastGlobalComputationTime;

    protected int lastLocalComputationTime;
    protected int [] globalCentralities = new int[EPOCH_COUNT];

    public CWindowCentrality_intf(Settings s) {
        if (s.contains(CENTRALITY_WINDOW_SETTING)) {
            CENTRALITY_TIME_WINDOW = s.getInt(CENTRALITY_WINDOW_SETTING);
        }

        if (s.contains(COMPUTATION_INTERVAL_SETTING)) {
            COMPUTE_INTERVAL = s.getInt(COMPUTATION_INTERVAL_SETTING);
        }

        if (s.contains(EPOCH_COUNT_SETTING)) {
            EPOCH_COUNT = s.getInt(EPOCH_COUNT_SETTING);
        }
    }

    public CWindowCentrality_intf(CWindowCentrality_intf proto) {
        // set these back in time (negative values) to do one computation at the 
        // start of the sim
        this.lastGlobalComputationTime = this.lastLocalComputationTime
                = -COMPUTE_INTERVAL;
    }

    @Override
    public double getGlobalCentrality(Map<DTNHost, List<Duration>> connHistory) {
        if (SimClock.getIntTime() - this.lastGlobalComputationTime < COMPUTE_INTERVAL) {
            return globalCentrality;
        }

        // initialize
        int[] centralities = new int[EPOCH_COUNT];
        int epoch, timeNow = SimClock.getIntTime();
        Map<Integer, Set<DTNHost>> nodesCountedInEpoch
                = new HashMap<Integer, Set<DTNHost>>();

        for (int i = 0; i < EPOCH_COUNT; i++) {
            nodesCountedInEpoch.put(i, new HashSet<DTNHost>());
        }

        /*
		 * For each node, loop through connection history until we crossed all
		 * the epochs we need to cover
         */
        int epochControl = 0;
        for (Map.Entry<DTNHost, List<Duration>> entry : connHistory.entrySet()) {
            DTNHost h = entry.getKey();
            for (Duration d : entry.getValue()) {
                int timePassed = (int) (timeNow - d.end);

                // if we reached the end of the last epoch, we're done with this node
//				System.out.println("EPOCH_COUNT "+EPOCH_COUNT);
//				System.out.println("time now: "+timeNow);
//				System.out.println("timePassed: "+timePassed);
                if (timePassed > CENTRALITY_TIME_WINDOW * EPOCH_COUNT)//{
                //System.out.println("break\n");
                {
                    break;
                }
                //	}
                // compute the epoch this contact belongs to
                epoch = timePassed / CENTRALITY_TIME_WINDOW;
                if (epoch > epochControl) {
                    epochControl = epoch;
                }
//				System.out.println("epoch: "+epoch);

                // Only consider each node once per epoch
                Set<DTNHost> nodesAlreadyCounted = nodesCountedInEpoch.get(epoch);
//				System.out.println("nodesAlreadyCounted: "+nodesAlreadyCounted);
//				System.out.println("h: "+h);
//				if(nodesAlreadyCounted!=null)
//					System.out.println("nodesAlreadyCounted.contains(h): "+nodesAlreadyCounted.contains(h));
//				else
//					System.out.println("nodesAlreadyCounted.contains(h): NULL");
//				if(nodesAlreadyCounted!=null && nodesAlreadyCounted.contains(h)){ 
                if (nodesAlreadyCounted.contains(h)) //				{	
                //					System.out.println("continue\n");
                {
                    continue;
                }
//				}
                // increment the degree for the given epoch
//				System.out.println("centralities["+epoch+"]: " + centralities[epoch] + ", size: " + centralities.length);
//				System.out.println("epoch: "+epoch);
                centralities[epoch]++;
                nodesAlreadyCounted.add(h);
//				System.out.println( "Add "+h+ " to nodesAlreadyCounted:"+nodesAlreadyCounted +"\n");
            }
        }
//		System.out.println("SAINDO PRIMEIRO FOR");

        // compute and return average node degree
        int control = 0, sum = 0;
        for (int i = 0; i < epochControl + 1; i++) { // CHANGE FROM for(int i = 0; i < EPOCH_COUNT; i++){ 
//			System.out.println("centralities["+i+"]: "+centralities[i]);
            sum += centralities[i];
            control++;
        }
//		System.out.println("epochControl: "+ epochControl);
//		System.out.println("control: "+ control);
        this.globalCentrality = ((double) sum) / control; // CHANGED FROM this.globalCentrality = ((double)sum) / EPOCH_COUNT;
//		System.out.println(this+".globalCentrality: "+this.globalCentrality+"\n");

        this.lastGlobalComputationTime = SimClock.getIntTime();

        return this.globalCentrality;
    }

    @Override
    public double getLocalCentrality(Map<DTNHost, List<Duration>> connHistory, CommunityDetection cd) {
        if (SimClock.getIntTime() - this.lastLocalComputationTime < COMPUTE_INTERVAL) {
            return localCentrality;
        }

        // centralities will hold the count of unique encounters in each epoch
        int[] centralities = new int[EPOCH_COUNT];
        int epoch, timeNow = SimClock.getIntTime();
        Map<Integer, Set<DTNHost>> nodesCountedInEpoch
                = new HashMap<Integer, Set<DTNHost>>();

        int epochControl = 0;

        for (int i = 0; i < EPOCH_COUNT; i++) {
            nodesCountedInEpoch.put(i, new HashSet<DTNHost>());
        }

        // local centrality only considers nodes in the local community
        Set<DTNHost> community = cd.getLocalCommunity();

        /*
		 * For each node, loop through connection history until we crossed all
		 * the epochs we need to cover
         */
        for (Map.Entry<DTNHost, List<Duration>> entry : connHistory.entrySet()) {
            DTNHost h = entry.getKey();

            // if the host isn't in the local community, we don't consider it
            if (!community.contains(h)) {
                continue;
            }

            for (Duration d : entry.getValue()) {
                int timePassed = (int) (timeNow - d.end);

                // if we reached the end of the last epoch, we're done with this node
                if (timePassed > CENTRALITY_TIME_WINDOW * EPOCH_COUNT) {
                    break;
                }

                // compute the epoch this contact belongs to
                epoch = timePassed / CENTRALITY_TIME_WINDOW;
                if (epoch > epochControl) {
                    epochControl = epoch;
                }

                // Only consider each node once per epoch
                Set<DTNHost> nodesAlreadyCounted = nodesCountedInEpoch.get(epoch);
                if (nodesAlreadyCounted.contains(h)) {
                    continue;
                }

                // increment the degree for the given epoch
//				System.out.println("epoch: "+epoch);
                centralities[epoch]++;
                nodesAlreadyCounted.add(h);
            }
        }
//		System.out.println("SAINDO PRIMEIRO FOR");
        // compute and return average node degree
        int control = 0, sum = 0;
        for (int i = 0; i < epochControl + 1; i++) {
//			System.out.println("centralities["+i+"]: "+centralities[i]);
            sum += centralities[i];
            control++;
        }
//		System.out.println("epochControl: "+ epochControl);
//		System.out.println("control: "+ control);

        this.localCentrality = ((double) sum) / control;
//		System.out.println(this+".localCentrality: "+this.localCentrality+"\n");

        this.lastLocalComputationTime = SimClock.getIntTime();

        return this.localCentrality;
    }

    @Override
    public Centrality_intf replicate() {
        return new CWindowCentrality_intf(this);
    }

    public int[] getArrayGlobalCentrality(Map<DTNHost, List<Duration>> connHistory) {
        //if (SimClock.getIntTime() - this.lastGlobalComputationTime < COMPUTE_INTERVAL)
        //	return globalCentralities;

        //initialise
        int epochControl = 0;
        int[] centralities = new int[EPOCH_COUNT];

        int epoch;
        int timeNow = SimClock.getIntTime();
        Map<Integer, Set<DTNHost>> nodesCountedInEpoch = new HashMap<Integer, Set<DTNHost>>();

        for (int i = 0; i < EPOCH_COUNT; i++) {
            nodesCountedInEpoch.put(i, new HashSet<DTNHost>());
        }
        //end-initialisation

        for (Map.Entry<DTNHost, List<Duration>> entry : connHistory.entrySet()) {
            DTNHost h = entry.getKey();
            for (Duration d : entry.getValue()) {
                int timePassed = (int) (timeNow - d.end);
                if (timePassed > CENTRALITY_TIME_WINDOW * EPOCH_COUNT) {
                    break;
                }

                epoch = timePassed / CENTRALITY_TIME_WINDOW; // EPOCH NUMBER/LOCATION
                if (epoch>epochControl) {
                    epochControl = epoch;
                }
                Set<DTNHost> nodesAlreadyCounted = nodesCountedInEpoch.get(epoch); //Only consider each node counted 1 per epoch
                if (nodesAlreadyCounted.contains(h)) {
                    continue;
                }
                centralities[epoch]++;
                nodesAlreadyCounted.add(h);

            }

        }
        
        int control = 0;
        int sum = 0;
        
        for (int i = 0; i < epochControl+1; i++) {
            sum += centralities[i];
            control++;
        }
        
        this.globalCentrality = ((double)sum) / control;

        this.lastGlobalComputationTime = SimClock.getIntTime();
        return centralities;
    }


}
