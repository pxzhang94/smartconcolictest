package smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import smartconcolic.implementation.IImplementation;
import smartconcolic.implementation.SimulationImplementation;
import smartconcolic.rmc.SmartCFG;
import smartconcolic.utils.FileUtils;

public class GreedyWithKnowledge extends Algorithm {
	
	public GreedyWithKnowledge(SmartCFG cfg, IImplementation imple) {
		super(cfg, imple);
		node_number = cfg.getTransition_matrix().getRowDimension();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		testInfo = new TestInfo();
	}
	
	/**
	 * Implementation of greedy algorithm for smart concolic.
	 * 
	 * @return the test information
	 */
	public TestInfo runTest() throws Exception {		
		Map<List<Integer>, String[]> pathMap = getImplementation().getSymbolicExecutionCost(cfg);
		RealMatrix coefficient_matrix = cfg.getTransition_matrix().copy();
		for(int i = 0; i < node_number; i++)
			coefficient_matrix.setEntry(i, i, -1);
		
		//if there is unvisited nodes
		while((1000000 > testInfo.rm_number + testInfo.se_number) && !unvisited.isEmpty() && checkIfCanSolve(getImplementation())){
			//set values of the equation set
			double[] value = new double[node_number];
			for(int i = 0; i < node_number; i++){
				if(unvisited.contains(i))
					value[i] = -1;
				else 
					value[i] = 0;
			}		
			DecompositionSolver solver = new LUDecomposition(coefficient_matrix).getSolver();
			RealVector constant = new ArrayRealVector(value);
			RealVector solution = solver.solve(constant);
			double[] result = solution.toArray();
			
			//choose a method and a path by comparing the gain per cost between random testing and symbolic execution
			double gainPerCost = result[0];   //initialize as random testing
			List<Integer> chosenPath = new ArrayList<Integer>();
			chosenPath.add(-1);  //the path is -1 when random testing is chosen
			double se_cost = 0;  //store the chosen symbolic execution cost
			
			//compare with symbolic execution
			for(Entry<List<Integer>, String[]> entry : pathMap.entrySet()){
				List<Integer> tempPath = entry.getKey();
				int last_node = tempPath.get(tempPath.size() - 1);
				String[] val = entry.getValue();
				if(val[1] != "NULL"){
					double tempGPC = result[last_node] / Double.valueOf(val[0]);
					//choose the path with greater gain per cost
					if(tempGPC > gainPerCost){
						gainPerCost = tempGPC;
						chosenPath = tempPath;
						se_cost = Double.valueOf(val[0]);
					}
				}
			}	
			List<Integer> newPath = new ArrayList<>();
			if(-1 == chosenPath.get(0)){
				//random testing
				testInfo.rm_number++;
				newPath = getImplementation().randomTesting(cfg);;
				testInfo.totalCost = testInfo.totalCost + newPath.size() - 1;
			}
			else{
				//update the test information
				chosenPath = subPath(chosenPath);
				newPath = getImplementation().symbolicExecution(cfg, testInfo, chosenPath);
				testInfo.totalCost = testInfo.totalCost + newPath.size() - 1;
				
				int index = newPath.size() > chosenPath.size() ? chosenPath.size() : newPath.size();
				for(int i = 0; i < index; i++){
					if(newPath.get(i) != chosenPath.get(i)){
						String[] temp = getImplementation().getPathMap().get(chosenPath);
						temp[1] = "NULL";
						getImplementation().getPathMap().remove(chosenPath);
						pathMap.remove(chosenPath);
						getImplementation().getPathMap().put(chosenPath, temp);
						pathMap.put(chosenPath, temp);
					}
				}
			}
			//remove nodes are visited in this testcase
			removeVisitedNode(newPath);
		}
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
    	cfg.getTerminate_nodes().add(19);
    	IImplementation imple = new SimulationImplementation();
		GreedyWithKnowledge ga = new GreedyWithKnowledge(cfg, imple);
		ga.runTest();
	}
}
