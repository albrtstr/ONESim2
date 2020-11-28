/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Settings;
import core.SimScenario;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.CentralityDetectionEngine;
import routing.community.degreeCentrality_array;

/**
 *
 * @author ASUS
 */
public class DegreeCentrality_BubbleRap_Report extends Report {

    Map<DTNHost, Integer> value = new HashMap<DTNHost, Integer>();

    public DegreeCentrality_BubbleRap_Report() {
        Settings s = getSettings();
        init();
    }

    protected void init() {
        super.init();;
    }

    @Override
    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        Map<DTNHost, List<Integer>> arrayCentralities = new HashMap<DTNHost, List<Integer>>();
        for (DTNHost host : nodes) {
            MessageRouter r = host.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine dec = ((DecisionEngineRouter) r).getDecisionEngine();

            if (!(dec instanceof degreeCentrality_array)) {
                continue;
            }

            degreeCentrality_array arr = (degreeCentrality_array) dec;
            int[] a = arr.Centralitiess();

            //degreeCentrality_array cde = (degreeCentrality_array) dec;
            List<Integer> arrayku = new ArrayList<Integer>();
            for (int cent : a) {
                arrayku.add(cent);
            }
//            String cetak = "";
//            for (int i = 0; i < a.length; i++) {
//                cetak += a[i] + ", ";
//            }

//            write(host + ", " + cetak);

            arrayCentralities.put(host, arrayku);
        }
        //super.done();

        for (Map.Entry<DTNHost, List<Integer>> entry : arrayCentralities.entrySet()) {
            DTNHost a = entry.getKey();
            Integer b = a.getAddress();

            write("" + b + '\t' + "global" + '\t' + entry.getValue());
        }
        super.done();
    }
}
