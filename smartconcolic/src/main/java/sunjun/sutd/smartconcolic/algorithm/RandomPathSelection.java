package sunjun.sutd.smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import sunjun.sutd.smartconcolic.implementation.IImplementation;
import sunjun.sutd.smartconcolic.implementation.SimulationImplementation;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;
import sunjun.sutd.smartconcolic.utils.RandomUtils;

public class RandomPathSelection extends Algorithm {
	
	public RandomPathSelection(SmartCFG cfg, IImplementation imple) {
		super(cfg, imple);
		node_number = cfg.getTransition_matrix().getRowDimension();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		testInfo = new TestInfo();
	}
	
	/**
	 * Generate a path randomly to a terminate node.
	 * 
	 * @param current_node the begin node 
	 * 
	 * @return a path to a terminate node
	 */
	private List<Integer> getRandomPath(int current_node){
		List<Integer> path = new ArrayList<>();
		path.add(current_node);
		
		//if current node is not the terminate node, continue
		while(!cfg.getTerminate_nodes().contains(current_node)){
			current_node = getRandomState(path);
			path.add(current_node);
		}
		return path;
	}
	
	/**
	 * Randomly choose a state.
	 * 
	 * @param path the path have been searched before
	 * @return the next node
	 */
	private int getRandomState(List<Integer> path) {
		int current_node = path.get(path.size() - 1);
		List<Integer> nodeList = new ArrayList<>();
		double[] probabilityArray = cfg.getTransition_matrix().getRow(current_node);
		for (int node = 0; node < probabilityArray.length; node++) {
			if(probabilityArray[node] > 0){
				if(!path.contains(node))
					nodeList.add(node);
				else if(probabilityArray[node] == 1)  //if all the exit of loop is in the loop
					nodeList.add(node);
			}		
		}
		//randomly choose a state
		int random = RandomUtils.nextInt(0, nodeList.size() - 1);
		return nodeList.get(random);
	}
	
	/**
	 * Implementation of random path search method for symbolic execution.
	 * 
	 * @return the test information
	 */
	public TestInfo runTest() throws Exception {	
		Map<List<Integer>, String[]> pathMap = getImplementation().getSymbolicExecutionCost(cfg);
		//symbolic execution
		while(!unvisited.isEmpty() && checkTerminateNode(getImplementation(), pathSolved)){
			List<Integer> chosenPath = getRandomPath(cfg.getInit_node());

			chosenPath = subPath(chosenPath);
			if(!pathSolved.contains(chosenPath)){	
				List<Integer> path = getImplementation().symbolicExecution(cfg, testInfo, chosenPath);
				testInfo.totalCost = testInfo.totalCost + path.size() - 1;
				pathSolved.add(chosenPath);
				removeVisitedNode(path);
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
    	RandomPathSelection rps = new RandomPathSelection(cfg,imple);
    	
    	System.out.println(rps.runTest());
	}

}
