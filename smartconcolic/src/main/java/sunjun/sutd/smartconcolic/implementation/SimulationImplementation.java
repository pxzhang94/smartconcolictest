package sunjun.sutd.smartconcolic.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.AllDirectedPaths;
import org.jgrapht.graph.DefaultWeightedEdge;

import cfgcoverage.jacoco.analysis.data.CFG;
import sunjun.sutd.smartconcolic.algorithm.TestInfo;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;
import sunjun.sutd.smartconcolic.utils.RandomUtils;

public class SimulationImplementation implements IImplementation {

	private Map<List<Integer>, String[]> pathMap = new HashMap<>();
	
	@Override
	public List<Integer> randomTesting(SmartCFG cfg) {
		int init_node = cfg.getInit_node();
		List<Integer> chosenPath = new ArrayList<>();
		if(cfg.getTerminate_nodes().contains(init_node))
			chosenPath.add(init_node);
		else
			chosenPath = getRandomPath(cfg, init_node);
		return chosenPath;
		
	}
	
	/**
	 * Randomly choose a state after current node.
	 * 
	 * @param current_node the node the path just arrived 
	 * 
	 * @return the next node
	 */
	public int getRandomState(SmartCFG cfg, int current_node) {
		int node = 0;
		double[] probabilityArray = cfg.getTransition_matrix().getRow(current_node);
		double random = RandomUtils.nextDouble();
		double probability = 0.0;
		//randomly choose a state
		for (; node < probabilityArray.length; node++) {
			probability += probabilityArray[node];
			//it is sure the circle will break because current node is not terminate node
			if(random < probability){
				 break;
			}
		}
		return node;
	}
	
	/**
	 * Generate a path randomly to a terminate node.
	 * 
	 * @param cfg the program's cfg
	 * @param current_node the begin node 
	 * 
	 * @return a path to a terminate node
	 */
	public List<Integer> getRandomPath(SmartCFG cfg, int current_node){
		List<Integer> path = new ArrayList<>();
		path.add(current_node);
		
		//if current node is not the terminate node, continue
		while(!cfg.getTerminate_nodes().contains(current_node)){
			current_node = getRandomState(cfg, current_node);
			path.add(current_node);
		}
		return path;
	}
	
	@Override
	public List<Integer> symbolicExecution(SmartCFG cfg, TestInfo testInfo, List<Integer> path) {
		//be careful, the = and add method of list is shadow copy
		List<Integer> tempPath = new ArrayList<>();
		List<Integer> returnPath = new ArrayList<>();
		for(int i = 0; i < path.size(); i++){
			tempPath.add(path.get(i));
			returnPath.add(path.get(i));
		}
		
		testInfo.se_number++;
		testInfo.totalCost += getPathSymbolicExecutionCost(cfg, tempPath);
		testInfo.se_paths.add(tempPath);
		
		int last_node = path.get(path.size() - 1);
		if(!cfg.getTerminate_nodes().contains(last_node)){
			List<Integer> nextPath = getRandomPath(cfg, last_node);
			testInfo.totalCost = testInfo.totalCost + nextPath.size() - 1;
			for(int i = 1; i < nextPath.size(); i++)
				returnPath.add(nextPath.get(i));
		}
		return returnPath;
	}

	@Override
	public Map<List<Integer>, String[]> getSymbolicExecutionCost(SmartCFG cfg) {
		// TODO Auto-generated method stub
		int init_node = cfg.getInit_node();
		
		//find the minimum cost of each pair between init_node and others
		for(int i = 0; i < cfg.getTransition_matrix().getRowDimension(); i++){
			//the path begin and end with the same node has no meaning
			if(i != init_node){
				List<List<Integer>> allPaths = getAllConnectingPaths(cfg, i);
				for(List<Integer> path : allPaths){
					double cost = getPathSymbolicExecutionCost(cfg, path);
					String[] temp = new String[]{String.valueOf(cost), "dontKnow"};	
					pathMap.put(path, temp);
				}
			}
		}
		return pathMap;
	}
	
	/**
	 * Get all the connecting paths between two nodes.
	 * 
	 * @param cfg
	 * @param target_node the end node
	 * 
	 * @return all the paths between source_node and target_node
	 */
	public List<List<Integer>> getAllConnectingPaths(SmartCFG cfg, int target_node) {
		int source_node = cfg.getInit_node();
		List<List<Integer>> connecting_paths = new ArrayList<>();
		AllDirectedPaths<Integer, DefaultWeightedEdge> adp = new AllDirectedPaths<>(cfg.getProb_graph());
		List<GraphPath<Integer, DefaultWeightedEdge>> paths = adp.getAllPaths(Integer.valueOf(source_node), Integer.valueOf(target_node), true, null);
		for(GraphPath<Integer, DefaultWeightedEdge> graphPath : paths){
			connecting_paths.add(Graphs.getPathVertexList(graphPath));
		}
		//the above method can't get the path connects two nodes directly
		if(cfg.getTransition_matrix().getEntry(source_node, target_node) > 0){
			List<Integer> path = new ArrayList<>();
			path.add(source_node);
			path.add(target_node);
			connecting_paths.add(path);
		}
		return connecting_paths;
	}
	
	/**
	 * Generate the cost for symbolic execution of specified path.
	 * 
	 * @param cfg
	 * @param path the specified path
	 * 
	 * @return the cost for symbolic execution
	 */
	public double getPathSymbolicExecutionCost(SmartCFG cfg, List<Integer> path){
		double cost = 0;
		for(int i=0; i<path.size()-1; i++){
			int current_node = path.get(i);
			int next_node = path.get(i+1);
			cost += cfg.getCost_matrix().getEntry(current_node, next_node);
		}
		return cost;
	}

	@Override
	public Map<List<Integer>, String[]> getPathMap() {
		return this.pathMap;
	}
}
