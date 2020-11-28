/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.community;

import core.DTNHost;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ASUS
 */
public interface Centrality_intf extends Centrality{

    /**
     * Returns the computed global centrality based on the connection history
     * passed as an argument.
     *
     * @param connHistory Contact History on which to compute centrality
     * @return Value corresponding to the global centrality
     */
    public double getGlobalCentrality(Map<DTNHost, List<Duration>> connHistory);

    /**
     * Returns the computed local centrality based on the connection history and
     * community detection objects passed as parameters.
     *
     * @param connHistory Contact history on which to compute centrality
     * @param cd CommunityDetection object that knows the local community
     * @return Value corresponding to the local centrality
     */
    public double getLocalCentrality(Map<DTNHost, List<Duration>> connHistory,
            CommunityDetection cd);

    /**
     * Duplicates a Centrality object. This is a convention of the ONE to easily
     * create multiple instances of objects based on defined settings.
     *
     * @return A duplicate Centrality instance
     */
    public Centrality_intf replicate();

    public int[] getArrayGlobalCentrality(Map<DTNHost, List<Duration>> connHistory);
}
