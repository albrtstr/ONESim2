/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimScenario;
import java.util.HashMap;
import java.util.List;
import java.util.*;
import java.lang.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.help_average;

/**
 *
 * @author ASUS
 */
public class DistributedBubbleRap_interContactReport extends Report{
    
    Map<DTNHost, Double> averageValue = new HashMap<DTNHost, Double>();
    
    public void done(){
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        for (DTNHost host : nodes) {
            MessageRouter r = host.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine dec = ((DecisionEngineRouter)r).getDecisionEngine();
            
            if (!(dec instanceof help_average)) {
                continue;
            }
            
            help_average cd = (help_average) dec;
            double hitungAverage = cd.rataRata();
            
            averageValue.put(host, hitungAverage);
        }
        for (Map.Entry<DTNHost, Double> entry: averageValue.entrySet()) {
            String print = "";
            
            DTNHost key = entry.getKey();
            double val = entry.getValue();
            
            write("Node \n" + key + "\t" + "\n Contact Time = "+val);
        }
        super.done();
    }
}
