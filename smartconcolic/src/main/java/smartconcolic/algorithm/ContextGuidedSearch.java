package smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import smartconcolic.implementation.IImplementation;
import smartconcolic.implementation.SimulationImplementation;
import smartconcolic.rmc.SmartCFG;

public class ContextGuidedSearch extends Algorithm {

	public ContextGuidedSearch(SmartCFG cfg, IImplementation imple) {
		super(cfg, imple);
		node_number = cfg.getTransition_matrix().getRowDimension();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		testInfo = new TestInfo();
	}
	
	/**
	 * Get the maximum depth among all the given paths.
	 * 
	 * @param paths the given paths
	 * @return the maximum depth
	 */
	private int getDepth(List<List<Integer>> paths) {
		int depth = 0;
		for(List<Integer> path : paths)
			if(path.size() > depth)
				depth = path.size() - 1;
		return depth;
	}
	
	/**
	 * Implementation of context guided search.
	 * 
	 * @return the test information
	 */
	public TestInfo runTest() throws Exception{
		Map<List<Integer>, String[]> pathMap = getImplementation().getSymbolicExecutionCost(cfg);
		//begin with random testing
		List<Integer> rtPath = getImplementation().randomTesting(cfg); //start with a random testing
		removeVisitedNode(rtPath);
		testInfo.rm_number++;
		testInfo.totalCost = testInfo.totalCost + rtPath.size() - 1;
		
		List<List<Integer>> executionTree = new ArrayList<>();  
		executionTree.add(rtPath);
		int k = 1;
		int maxDepth = 1;
		for(Entry<List<Integer>, String[]> entry : pathMap.entrySet())
			if(entry.getKey().size() > maxDepth)
				maxDepth = entry.getKey().size();
		//if there is unvisited nodes
		while(k <= maxDepth && !unvisited.isEmpty()){
			Set<List<Integer>> context = new HashSet<>();
			int d = k - 1;
			int depth = getDepth(executionTree);
			//Breadth First Search, actually in our situation the context guided search is bfs
			while(d < depth && !unvisited.isEmpty()){
				List<List<Integer>> paths = new ArrayList<>();
				for(int i = 0; i < executionTree.size(); i++)
					paths.add(executionTree.get(i));
				
				for(List<Integer> path : paths){
					if(d < path.size() - 1){
						int b = path.get(d);  //get branches at depth d from execution tree
						List<Integer> tempContext = path.subList(d + 1 - k, d + 1);//get k-context of the branch
						if(!context.contains(tempContext)){
							context.add(tempContext);  //update the k-context
							
							//search for the negated path
							double[] probabilityArray = cfg.getTransition_matrix().getRow(b);
							for(int node = 0; node < probabilityArray.length; node++){
								if(probabilityArray[node] > 0 && node != path.get(d + 1)){
									//be careful, the = and add method of list is shadow copy
									List<Integer> tempPath = new ArrayList<>();
									for(int i = 0; i < d + 1; i++)
										tempPath.add(path.get(i));
									tempPath.add(node);
							        
									//update the test information
									tempPath = subPath(tempPath);
									if(!pathSolved.contains(tempPath)){
										List<Integer> finalPath = getImplementation().symbolicExecution(cfg, testInfo, tempPath);
										testInfo.totalCost = testInfo.totalCost + finalPath.size() - 1;
										pathSolved.add(tempPath);
										//remove nodes are visited in this testcase
										removeVisitedNode(finalPath);
										//update the execution tree
										if(finalPath.size() == 1)
											executionTree.add(tempPath);
										else
											executionTree.add(finalPath);
										depth = getDepth(executionTree);
									}
								}
							}	
						}
					}
						
				}
				d++;
			}
			k++;
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
		ContextGuidedSearch cgs = new ContextGuidedSearch(cfg, imple);
		System.out.println(cgs.runTest());	
	}

}
