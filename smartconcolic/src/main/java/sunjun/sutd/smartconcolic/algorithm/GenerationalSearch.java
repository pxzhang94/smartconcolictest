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

public class GenerationalSearch extends Algorithm {

	public GenerationalSearch(SmartCFG cfg, IImplementation imple) {
		super(cfg, imple);
		node_number = cfg.getTransition_matrix().getRowDimension();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		testInfo = new TestInfo();
	}

	/**
	 * Expand execution from given path.
	 * 
	 * @param input the given path
	 * 
	 * @return all the paths generated from given path
	 */
	private List<Path> expandExecution(Path input) {
		List<Path> childInputs = new ArrayList<>();  //store the paths generated from given path
		
		//search from the top node which have not been searched before
		for(int j = input.bound; j < input.path.size() - 1; j++){
			double[] probabilityArray = cfg.getTransition_matrix().getRow(input.path.get(j));
			for(int i = 0; i < probabilityArray.length; i++){
				//the node been visited before only have two situation:1.has been searched,2.the path contains it in the workList 
				if(probabilityArray[i] > 0 && i != input.path.get(j + 1) && unvisited.contains(i)){
					List<Integer> tempPath = new ArrayList<>();
					for(int index = 0; index < j + 1; index++)
						tempPath.add(input.path.get(index));
					tempPath.add(i);
					
					Path newInput = new Path(tempPath, j + 1);
					childInputs.add(newInput);
				}
				
			}
		}
		return childInputs;
	}
	
	/**
	 * Implementation of directed search method which is achieved in Automated Whitebox Fuzz Testing.
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
		Path inputSeed = new Path(path, 0);
		
		List<Path> workList = new ArrayList<>();
		workList.add(inputSeed);
		
		//if there is unvisited nodes
		while(!workList.isEmpty() && !unvisited.isEmpty()){
			Path input = workList.get(0);
			workList.remove(0);
			List<Path> childInputs = expandExecution(input);
			if(!childInputs.isEmpty()){
				for(int i = 0; i < childInputs.size(); i++){
					Path newInput = childInputs.get(i);
					List<Integer> newPath = newInput.path;
					
					//update the test information
					newPath = getImplementation().symbolicExecution(cfg, testInfo, newPath);
					testInfo.totalCost = testInfo.totalCost + newPath.size() - 1;
					
					//remove nodes are visited in this testcase
					removeVisitedNode(newPath);
					newInput.path = newPath;
					
					workList.add(newInput);
				}
			}
		}
		testInfo.coverageRatio = 1 - (double)unvisited.size() / node_number;
		//print the test information
		return testInfo;
	}

	public static void main(String[] args) throws Exception {
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
		
    	SmartCFG cfg = new SmartCFG(transition_matrix_2, cost_matrix_2, Integer.valueOf(0), ten2, ln2);
    	IImplementation imple = new SimulationImplementation();
    	
    	for(int i = 0; i < 100; i++){
    		GenerationalSearch gs = new GenerationalSearch(cfg, imple);
    		System.out.println(gs.runTest());
    	}
	}
}

class Path {
	List<Integer> path = new ArrayList<>();
	int bound;  //the top node in the path which have not been searched before
	
	public Path(List<Integer> path, int bound) {
		//be careful, the = and add method of list is shadow copy
		for(int i = 0; i < path.size(); i++)
			this.path.add(path.get(i));
		this.bound = bound;
	}
}
