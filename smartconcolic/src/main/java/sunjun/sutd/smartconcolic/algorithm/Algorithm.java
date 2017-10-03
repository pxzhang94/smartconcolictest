package sunjun.sutd.smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.RealMatrix;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import sunjun.sutd.smartconcolic.implementation.IImplementation;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;

public abstract class Algorithm {
	
	private IImplementation implementation;
	
	protected SmartCFG cfg;
	List<List<Integer>> pathSolved = new ArrayList<>();
	int node_number;
	Set<Integer> unvisited = new HashSet<Integer>();  //store the nodes have not been visited in the past testing
	RealMatrix coverage_matrix;  //store the number of testcases cover each pair of nodes
	TestInfo testInfo;  //store all the information of the test
	
	public Algorithm(SmartCFG cfg, IImplementation imple){
		this.cfg = cfg;
		this.implementation = imple;
	}
	
	/**
	 * Generate the probability of specified path.
	 * 
	 * @param path the specified path
	 * 
	 * @return the probability
	 */
	public double getPathProbability(List<Integer> path) {
		double prob = 1;
		for(int i=0; i<path.size()-1; i++){
			int current_node = path.get(i);
			int next_node = path.get(i+1);
			prob *= cfg.getTransition_matrix().getEntry(current_node, next_node);
		}
		return prob;
	}

	/**
	 * Remove the node in last path.
	 * 
	 * @param path the last testing path
	 */
	public void removeVisitedNode(List<Integer> path) {
		for(int i = 0; i < path.size(); i++)
			unvisited.remove(path.get(i));
	}
	
	public List<Integer> subPath(List<Integer> oldPath) {
		int index = oldPath.size() - 2;
		List<Integer> newPath = new ArrayList<>();
		for(; index > 0; index--){
			if(cfg.getBranch_nodes().contains(oldPath.get(index)))
				break;
		}
		for(int i = 0; i < index + 2; i++)
			newPath.add(oldPath.get(i));
		return newPath;
	}
	
	public boolean checkIfContinue(IImplementation imple, List<List<Integer>> pathSolved) {
		for(int node : unvisited){
			List<List<Integer>> paths = imple.getAllConnectingPaths(cfg, node);
			for(List<Integer> path : paths){
				path = subPath(path);
				if(!pathSolved.contains(path))
					return true;
			}
		}
		return false;
	}
	
	public boolean checkIfCanSolve(IImplementation imple) {
		Map<List<Integer>, String[]> pathMap = imple.getPathMap();
		for(int node : unvisited){
			List<List<Integer>> paths = imple.getAllConnectingPaths(cfg, node);
			for(List<Integer> path : paths){
				path = subPath(path);
				if(pathMap.containsKey(path) && pathMap.get(path)[1] != "NULL")
					return true;
			}
		}
		return false;
	}
	
	public boolean checkTerminateNode(IImplementation imple, List<List<Integer>> pathSolved) {
		for(int node : cfg.getTerminate_nodes()){
			List<List<Integer>> paths = imple.getAllConnectingPaths(cfg, node);
			for(List<Integer> path : paths){
				path = subPath(path);
				if(!pathSolved.contains(path))
					return true;
			}
		}
		return false;
	}

	/**
	 * Implementation of testing method.
	 * 
	 * @return the test information
	 * 
	 * @throws Exception
	 */
	public TestInfo runTest() throws Exception{
		return null;
	};
	
	public IImplementation getImplementation() {
		return implementation;
	}

	public void setImplementation(IImplementation implementation) {
		this.implementation = implementation;
	}
}
