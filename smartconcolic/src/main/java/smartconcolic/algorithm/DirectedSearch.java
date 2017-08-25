package smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import smartconcolic.implementation.IImplementation;
import smartconcolic.implementation.SimulationImplementation;
import smartconcolic.rmc.SmartCFG;


public class DirectedSearch extends Algorithm {
	
	public DirectedSearch(SmartCFG cfg, IImplementation imple) {
		super(cfg, imple);
		node_number = cfg.getTransition_matrix().getRowDimension();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		testInfo = new TestInfo();

	}
	
	/**
	 * Implementation of directed search method which is achieved in Dart algorithm.
	 * 
	 * @return the test information
	 */
	public TestInfo runTest() throws Exception {
		Map<List<Integer>, String[]> pathMap = getImplementation().getSymbolicExecutionCost(cfg);
		//begin with random testing
		List<Integer> path = getImplementation().randomTesting(cfg); //start with a random testing
		removeVisitedNode(path);
		testInfo.rm_number++;
		testInfo.totalCost = testInfo.totalCost + path.size() - 1;
		
		//if there is unvisited nodes
		while(checkIfContinue(getImplementation(), pathSolved) && !unvisited.isEmpty() && path.size() >= 2){
			int node_index = path.size() - 1;
			int last_node = path.get(node_index);
			path.remove(node_index--);  //remove the last node of path
			int current_node = path.get(node_index);  //the node will search for other branches
			
			double[] probabilityArray = cfg.getTransition_matrix().getRow(current_node);
			Set<Integer> candidate = new HashSet<>();
			candidate.add(last_node);
			for(int node = 0; node < probabilityArray.length; node++){
				//only focus on the node unvisited, because it is depth first search
				if(probabilityArray[node] > 0 && node != last_node){
					candidate.add(node);
					path.add(node);
					
					//update the test information
					List<Integer> newPath = subPath(path);
					if(!pathSolved.contains(newPath)){
						List<Integer> tempPath = getImplementation().symbolicExecution(cfg, testInfo, newPath);
						testInfo.totalCost = testInfo.totalCost + tempPath.size() - 1;
						pathSolved.add(newPath);
						
						removeVisitedNode(tempPath);
						if(tempPath.size() > path.size()){
							path.clear();
							for(int i = 0; i < tempPath.size(); i++)
								path.add(i, tempPath.get(i));
						}
					}
				}
			}	
			if(candidate.size() > 1){
				path.remove(path.size() - 1);
				path = subPath(path);
			}
		}
		testInfo.coverageRatio = 1 - (double)unvisited.size() / node_number;
		//print the test information
		return testInfo;
	}
	
	public static void main(String[] args) throws Exception {
    	RealMatrix transition_matrix = new OpenMapRealMatrix(8, 8);
    	RealMatrix cost_matrix = new OpenMapRealMatrix(8, 8);
    	
    	transition_matrix.setEntry(0, 1, 0.5);
    	transition_matrix.setEntry(1, 2, 1);
    	transition_matrix.setEntry(0, 2, 0.5);
    	transition_matrix.setEntry(2, 3, 0.5);
    	transition_matrix.setEntry(2, 4, 0.5);
    	transition_matrix.setEntry(4, 7, 0.5);
    	transition_matrix.setEntry(4, 5, 0.5);
    	transition_matrix.setEntry(5, 7, 0.5);
    	transition_matrix.setEntry(5, 6, 0.5);
    	transition_matrix.setEntry(6, 7, 1);
    	
    	cost_matrix.setEntry(0, 1, 5);
    	cost_matrix.setEntry(0, 2, 5);
    	cost_matrix.setEntry(2, 3, 20);
    	cost_matrix.setEntry(2, 4, 20);
    	cost_matrix.setEntry(4, 7, 50);
    	cost_matrix.setEntry(4, 5, 50);
    	cost_matrix.setEntry(5, 7, 10);
    	cost_matrix.setEntry(5, 6, 10);
    	Set<Integer> ten = new HashSet<Integer>();
    	Set<Integer> ln = new HashSet<Integer>();
    	ten.add(Integer.valueOf(3));
    	ten.add(Integer.valueOf(7));
    	SmartCFG cfg = new SmartCFG(transition_matrix, cost_matrix, Integer.valueOf(0), ten, ln);
    	
    	IImplementation imple = new SimulationImplementation();
		DirectedSearch ds = new DirectedSearch(cfg, imple);
		System.out.println(ds.runTest());
		
	}
}
