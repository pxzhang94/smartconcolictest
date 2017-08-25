package smartconcolic.algorithm;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import smartconcolic.implementation.IImplementation;
import smartconcolic.rmc.SmartCFG;

public class RandomTesting extends Algorithm {

	public RandomTesting(SmartCFG cfg, IImplementation imple) {
		super(cfg, imple);
		node_number = cfg.getTransition_matrix().getRowDimension();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		testInfo = new TestInfo();
	}
	
	/**
	 * Implementation of random testing.
	 * 
	 * @return the test information
	 */
	public TestInfo runTest() throws Exception {
		Map<List<Integer>, String[]> pathMap = getImplementation().getSymbolicExecutionCost(cfg);
		//if there is unvisited nodes
		while(!unvisited.isEmpty() && testInfo.rm_number <= 1000000){
			//random testing
			List<Integer> path = getImplementation().randomTesting(cfg);
			removeVisitedNode(path);
			testInfo.rm_number++;
			testInfo.totalCost = testInfo.totalCost + path.size() - 1;
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
    	
//		RandomTesting rt = new RandomTesting(cfg);
//		rt.runTest();
		
	}

}
