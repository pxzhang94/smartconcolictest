package sunjun.sutd.smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import sunjun.sutd.smartconcolic.implementation.IImplementation;
import sunjun.sutd.smartconcolic.implementation.SimulationImplementation;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;

public class DepthFirstSearch extends Algorithm {
	
	public DepthFirstSearch(SmartCFG cfg, IImplementation imple) {
		super(cfg, imple);
		node_number = cfg.getTransition_matrix().getRowDimension();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		testInfo = new TestInfo();
	}
	
	/**
	 * Recursion method to get the symbolic execution path.
	 * 
	 * @param current_node the current node path just arrive
	 * @param pastPath the past path have been passed
	 */
	private void dfsRecursion(int current_node, List<Integer> pastPath) {
		//be careful, the = and add method of list is shadow copy
		List<Integer> path = new ArrayList<>();
		for(int i = 0; i < pastPath.size(); i++)
			path.add(pastPath.get(i));
		
		//our goal is to cover all the state
		while(!unvisited.isEmpty() && checkIfContinue(getImplementation(), pathSolved)){
			path.add(current_node);

			//only if the node is not the terminate node, the path will go on
			if(!cfg.getTerminate_nodes().contains(current_node)){
				//if the last node in unvisited set is not the terminate node
				if(unvisited.isEmpty() || !checkIfContinue(getImplementation(), pathSolved)){
					//get the whole path from init_node to terminate node by dfs
					while(!cfg.getTerminate_nodes().contains(current_node)){
						double[] probabilityArray = cfg.getTransition_matrix().getRow(current_node);
						for(int next_node = 0; next_node < probabilityArray.length; next_node++){
							if(probabilityArray[next_node] > 0){
								path.add(next_node);
								current_node = next_node;
								break;
							}
						}
					}
					//update the test information
					path = subPath(path);
					if(!pathSolved.contains(path)){	
						List<Integer> newPath = getImplementation().symbolicExecution(cfg, testInfo, path);
						testInfo.totalCost = testInfo.totalCost + newPath.size() - 1;
						pathSolved.add(path);
						removeVisitedNode(newPath);
					}
					break;
				}
				else{
					double[] probabilityArray = cfg.getTransition_matrix().getRow(current_node);
					int next_node = 0;
					List<Integer> candidate = new ArrayList<>();
					
					for(; next_node < probabilityArray.length; next_node++){
						if(probabilityArray[next_node] > 0){
							candidate.add(next_node);
							dfsRecursion(next_node, path);
						}
					}	
					path.add(candidate.get(0));
					if(candidate.size() > 1){
						Map<List<Integer>, String[]> pathMap = getImplementation().getPathMap();
						if(!pathMap.containsKey(path) || (pathMap.containsKey(path) && pathMap.get(path)[1] == "NULL")){
							path.remove(path.size() - 1);
							path = subPath(path);
							if(!pathSolved.contains(path)){	
								pathSolved.add(path);
								if(path.size() > 2){
									List<Integer> newPath = getImplementation().symbolicExecution(cfg, testInfo, path);
									testInfo.totalCost = testInfo.totalCost + newPath.size() - 1;
									removeVisitedNode(newPath);
								}
							}
						}
						else{
							path.remove(path.size() - 1);
							path = subPath(path);
							if(!pathSolved.contains(path))	
								pathSolved.add(path);
						}
							
					}
					break;
				}
			}
			else{
				//update the test information
				path = subPath(path);
				if(!pathSolved.contains(path)){	
					List<Integer> newPath = getImplementation().symbolicExecution(cfg, testInfo, path);
					testInfo.totalCost = testInfo.totalCost + newPath.size() - 1;
					pathSolved.add(path);
					removeVisitedNode(newPath);
				}
				break;
			}
		}	
	}
	
	/**
	 * Implementation of depth first search method for symbolic execution.
	 * 
	 * @return the test information
	 */
	public TestInfo runTest() throws Exception {
		Map<List<Integer>, String[]> pathMap = getImplementation().getSymbolicExecutionCost(cfg);
		//symbolic execution
		int current_node = cfg.getInit_node();
		List<Integer> path = new ArrayList<>();
		dfsRecursion(current_node, path);
		
		testInfo.coverageRatio = 1 - (double)unvisited.size() / node_number;
		//print the test information
		return testInfo;
	}
	
	public static void main(String[] args) throws Exception {
//    	RealMatrix transition_matrix_1 = new OpenMapRealMatrix(6, 6);
//    	RealMatrix cost_matrix_1 = new OpenMapRealMatrix(6, 6);
//    	
//    	transition_matrix_1.setEntry(0, 1, 0.5);
//    	transition_matrix_1.setEntry(1, 2, 1);
//    	transition_matrix_1.setEntry(0, 5, 0.5);
//    	transition_matrix_1.setEntry(2, 3, 0.5);
//    	transition_matrix_1.setEntry(2, 4, 0.5);
//    	transition_matrix_1.setEntry(5, 2, 1);
//    	
//    	cost_matrix_1.setEntry(0, 1, 5);
//    	cost_matrix_1.setEntry(0, 5, 5);
//    	cost_matrix_1.setEntry(2, 3, 20);
//    	cost_matrix_1.setEntry(2, 4, 20);
//    	Set<Integer> ten1 = new HashSet<Integer>();
//    	Set<Integer> ln1 = new HashSet<Integer>();
//    	ten1.add(Integer.valueOf(3));
//    	ten1.add(4);
//    	SmartCFG cfg1 = new SmartCFG(transition_matrix_1, cost_matrix_1, Integer.valueOf(0), ten1, ln1);
//    	
    	IImplementation imple = new SimulationImplementation();
//    	DepthFirstSearch dfs1 = new DepthFirstSearch(cfg1, imple);
//    	dfs1.runTest();
    	
    	RealMatrix transition_matrix_2 = new OpenMapRealMatrix(8, 8);
    	RealMatrix cost_matrix_2 = new OpenMapRealMatrix(8, 8);
    	
    	transition_matrix_2.setEntry(0, 1, 0.5);
    	transition_matrix_2.setEntry(1, 2, 1);
    	transition_matrix_2.setEntry(0, 2, 0.5);
    	transition_matrix_2.setEntry(2, 3, 0.5);
    	transition_matrix_2.setEntry(2, 4, 0.5);
    	transition_matrix_2.setEntry(4, 7, 0.5);
    	transition_matrix_2.setEntry(4, 5, 0.5);
    	transition_matrix_2.setEntry(5, 7, 0.5);
    	transition_matrix_2.setEntry(5, 6, 0.5);
    	transition_matrix_2.setEntry(6, 7, 1);
    	
    	cost_matrix_2.setEntry(0, 1, 5);
    	cost_matrix_2.setEntry(0, 2, 5);
    	cost_matrix_2.setEntry(2, 3, 20);
    	cost_matrix_2.setEntry(2, 4, 20);
    	cost_matrix_2.setEntry(4, 7, 50);
    	cost_matrix_2.setEntry(4, 5, 50);
    	cost_matrix_2.setEntry(5, 7, 10);
    	cost_matrix_2.setEntry(5, 6, 10);
    	Set<Integer> ten2 = new HashSet<Integer>();
    	Set<Integer> ln2 = new HashSet<Integer>();
    	ten2.add(Integer.valueOf(3));
    	ten2.add(Integer.valueOf(7));
		
    	SmartCFG cfg2 = new SmartCFG(transition_matrix_2, cost_matrix_2, Integer.valueOf(0), ten2, ln2);
    	
    	DepthFirstSearch dfs2 = new DepthFirstSearch(cfg2, imple);
    	System.out.println(dfs2.runTest());
	}

}
