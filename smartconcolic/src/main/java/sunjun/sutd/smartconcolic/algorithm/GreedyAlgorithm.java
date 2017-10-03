package sunjun.sutd.smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import sunjun.sutd.smartconcolic.implementation.IImplementation;
import sunjun.sutd.smartconcolic.implementation.SimulationImplementation;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;
import sunjun.sutd.smartconcolic.utils.FileUtils;

public class GreedyAlgorithm extends Algorithm {
	
	public GreedyAlgorithm(SmartCFG cfg, IImplementation imple) {
		super(cfg, imple);
		node_number = cfg.getTransition_matrix().getRowDimension();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		coverage_matrix = new OpenMapRealMatrix(node_number, node_number); 
		testInfo = new TestInfo();
	}
	
	/**
	 * Get the estimation matrix after each test.
	 * 
	 * @return the new estimation Matrix
	 */
	private RealMatrix getEstimationMatrix() {
		RealMatrix uniform_matrix = new OpenMapRealMatrix(node_number, node_number);		
		for(int i = 0; i < node_number; i++){
			double sum = 0;
			//calculate the #s in laplace estimator
			for(int j = 0; j < node_number; j++){
				sum += coverage_matrix.getEntry(i, j);
			}
			//calculate the (#(s,e,t) + 1) / (#s + n) in laplace estimator
			for(int j = 0; j < node_number; j++){
				if(cfg.getTransition_matrix().getEntry(i, j) > 0)
					uniform_matrix.setEntry(i, j, (coverage_matrix.getEntry(i, j) + 1) / (sum + cfg.getTransitions_per_row()[i]));
			}
			uniform_matrix.setEntry(i, i, -1);
		}
		return uniform_matrix;
	}
	
	/**
	 * Implementation of greedy algorithm for smart concolic.
	 * 
	 * @return the test information
	 */
	public TestInfo runTest() throws Exception {		
		Map<List<Integer>, String[]> pathMap = getImplementation().getSymbolicExecutionCost(cfg);
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
			DecompositionSolver solver = new LUDecomposition(getEstimationMatrix()).getSolver();
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
			
			//update coverage matrix
			for(int i = 0; i < newPath.size() - 1; i++){
				int current_node = newPath.get(i);
				int next_node = newPath.get(i+1);
				coverage_matrix.addToEntry(current_node, next_node, 1.0);
			}
		}
		testInfo.coverageRatio = 1 - (double)unvisited.size() / node_number;
		//print the test information
		return testInfo;
	}
	
	public static void main(String[] args) throws Exception {
    	RealMatrix transition_matrix = new OpenMapRealMatrix(8, 8);
    	RealMatrix cost_matrix = new OpenMapRealMatrix(8, 8);
    	
    	transition_matrix.setEntry(0, 1, 0.995);
    	transition_matrix.setEntry(1, 2, 1);
    	transition_matrix.setEntry(0, 2, 0.005);
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
//    	SmartCFG cfg = (SmartCFG)FileUtils.readObject("/Users/pxzhang/Downloads/SimulationTest/temp/20-17");
    	IImplementation imple = new SimulationImplementation();
		GreedyAlgorithm ga = new GreedyAlgorithm(cfg, imple);
		System.out.println(ga.runTest());
	}
}
