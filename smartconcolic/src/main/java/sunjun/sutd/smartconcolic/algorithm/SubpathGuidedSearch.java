package sunjun.sutd.smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.MersenneTwister;

import sunjun.sutd.smartconcolic.implementation.IImplementation;
import sunjun.sutd.smartconcolic.implementation.SimulationImplementation;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;
import sunjun.sutd.smartconcolic.utils.RandomUtils;

public class SubpathGuidedSearch extends Algorithm {

	int length;  //the number of nodes in every path stored in the priority queue
	Map<List<Integer>, Integer> priorityQueue = new HashMap<>();  //the priority queue to store all the subpath
	
	public SubpathGuidedSearch(SmartCFG cfg, IImplementation imple, int length) {
		super(cfg, imple);
		this.length = length;
		node_number = cfg.getTransition_matrix().getRowDimension();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		testInfo = new TestInfo();
	}
	
	/**
	 * Implementation of subpath guided search.
	 * 
	 * @return the test information
	 */
	public TestInfo runTest() {
		Map<List<Integer>, String[]> pathMap = getImplementation().getSymbolicExecutionCost(cfg);
		//initialization
		int init_node = cfg.getInit_node();
		List<CandidatePath> esVector = new ArrayList<>();
		
		//begin symbolic execution
		CandidatePath init_path = new CandidatePath(length);
		init_path.add(init_node);
		esVector.add(init_path);
		priorityQueue.put(esVector.get(0).subpath, 0);

		//if there is unvisited nodes and still have some pending paths
		while(esVector.size() > 0 && !unvisited.isEmpty()) {
			//select a pending path randomly, then update its frequency
			CandidatePath es = selectState(esVector);  
			int frequency = priorityQueue.get(es.subpath) + 1;
			priorityQueue.put(es.subpath, frequency);
			
			//next move of the chosen path
			List<Integer> path = es.path;
			int current_node = path.get(path.size() - 1);
			//if the last node is terminate node, do symbolic execution, 
			//otherwise, find next nodes to generate new pending paths 
			if(cfg.getTerminate_nodes().contains(current_node)){
				//symbolic execution
				path = subPath(path);
				List<Integer> newPath = getImplementation().symbolicExecution(cfg, testInfo, path);
				testInfo.totalCost = testInfo.totalCost + newPath.size() - 1;
				
				//remove nodes are visited in this testcase
				removeVisitedNode(newPath);
			}
			else{
				double[] probabilityArray = cfg.getTransition_matrix().getRow(current_node);
				for(int next_node = 0; next_node < probabilityArray.length; next_node++){
					if(probabilityArray[next_node] > 0){
						CandidatePath tempES = new CandidatePath(es);
						tempES.add(next_node);
						
						//frequency of the new subpath in priority queue initialized as 0
						if(!priorityQueue.containsKey(tempES.subpath))
							priorityQueue.put(tempES.subpath, 0);
						
						//add the new pending path into execution state vector
						esVector.add(tempES);
					}
				}
			}
			//remove the chosen pending path from execution state vector
			esVector.remove(es);
		}
		testInfo.coverageRatio = 1 - (double)unvisited.size() / node_number;
		//print the test information
		return testInfo;
	}
	
	/**
	 * Select a execution state(path) randomly.
	 * 
	 * @param esVector pending paths
	 * 
	 * @return the chosen path
	 */
	private CandidatePath selectState(List<CandidatePath> esVector) {
		List<CandidatePath> selectSet = new ArrayList<>();
		int lowest = Integer.MAX_VALUE;	  

		//focus on the subpath of pending paths
		for(int i = 0; i < esVector.size(); i++){
			int frequency = priorityQueue.get(esVector.get(i).subpath);
			if(frequency == lowest){
				selectSet.add(esVector.get(i));
			}
			else if(frequency < lowest){
				selectSet.clear();
				selectSet.add(esVector.get(i));
				lowest = frequency;
			}
		}
		//choose a pending path randomly
		int random = RandomUtils.nextInt(0, selectSet.size() - 1);
		return selectSet.get(random);
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
		SubpathGuidedSearch sgs = new SubpathGuidedSearch(cfg, imple, 1);
		System.out.println(sgs.runTest());
	}
}

/**
 * This class stores all the information of pending path we need for this algorithm.
 */
class CandidatePath {
	List<Integer> path = new ArrayList<>();
	List<Integer> subpath = new ArrayList<>();
	int length;
	
	public CandidatePath(int length) {
		this.length = length;
	}
	
	public CandidatePath(CandidatePath cp) {
		List<Integer> tempPath = cp.path;
		List<Integer> tempSubpath = cp.subpath;
		//be careful, the = and add method of list is shadow copy
		for(int i = 0; i < tempPath.size(); i++)
			this.path.add(tempPath.get(i));
		for(int i = 0; i < tempSubpath.size(); i++)
			this.subpath.add(tempSubpath.get(i));
		this.length = cp.length;
	}
	
	/**
	 * Update the parameter of this class when add a new node.
	 * 
	 * @param node the new node
	 */
	public void add(Integer node) {
		path.add(node);
		subpath.add(node);
		
		//if subpath's size is larger than given size, then remove the first node
		if(subpath.size() > length)
			subpath.remove(0);
	}
}