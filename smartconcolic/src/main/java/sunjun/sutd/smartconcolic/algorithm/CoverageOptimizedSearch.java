package sunjun.sutd.smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.AllDirectedPaths;
import org.jgrapht.graph.DefaultWeightedEdge;

import sunjun.sutd.smartconcolic.implementation.IImplementation;
import sunjun.sutd.smartconcolic.implementation.SimulationImplementation;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;
import sunjun.sutd.smartconcolic.utils.RandomUtils;

public class CoverageOptimizedSearch extends Algorithm {

	public CoverageOptimizedSearch(SmartCFG cfg, IImplementation imple) {
		super(cfg, imple);
		node_number = cfg.getTransition_matrix().getRowDimension();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		testInfo = new TestInfo();
	}
	
	public List<List<Integer>> getAllConnectingPaths(int source_node, int target_node) {
		List<List<Integer>> connecting_paths = new ArrayList<>();
		AllDirectedPaths<Integer, DefaultWeightedEdge> adp = new AllDirectedPaths<>(cfg.getProb_graph());
		List<GraphPath<Integer, DefaultWeightedEdge>> paths = adp.getAllPaths(Integer.valueOf(source_node), Integer.valueOf(target_node), true, null);
		for(GraphPath<Integer, DefaultWeightedEdge> graphPath : paths){
			connecting_paths.add(Graphs.getPathVertexList(graphPath));
		}
		//the above method can't get the path connects two nodes directly
		if(cfg.getTransition_matrix().getEntry(source_node, target_node) > 0){
			List<Integer> path = new ArrayList<>();
			path.add(source_node);
			path.add(target_node);
			connecting_paths.add(path);
		}
		return connecting_paths;
	}
	
	public TestInfo runTest() {
		Map<List<Integer>, String[]> pathMap = getImplementation().getSymbolicExecutionCost(cfg);
		Map<Integer, Node> visited = new HashMap<>();
		//begin with random testing
		List<Integer> rtPath = getImplementation().randomTesting(cfg); //start with a random testing
		removeVisitedNode(rtPath);
		testInfo.rm_number++;
		testInfo.totalCost = testInfo.totalCost + rtPath.size() - 1;
		for(Integer rtNode : rtPath)
			visited.put(rtNode, new Node(rtNode, rtPath));
		
		while(!unvisited.isEmpty()){
			int depth = Integer.MAX_VALUE;
			List<List<Integer>> paths = new ArrayList<>();
			for(Entry<Integer, Node> entry : visited.entrySet()){
				int node = entry.getKey();
				List<List<Integer>> candidatePaths = new ArrayList<>();
				for(int next_node = 0; next_node < node_number; next_node++){
					if(unvisited.contains(next_node)){
						candidatePaths = getAllConnectingPaths(node, next_node);
					}
				}
				for(List<Integer> candidatePath : candidatePaths){
					List<Integer> path = entry.getValue().generateNewPath(candidatePath);
					List<Integer> temp = subPath(path);
					if(!pathSolved.contains(temp)){
						if(candidatePath.size() < depth){
							paths.clear();
							depth = candidatePath.size();
							paths.add(path);
						}
						else if(candidatePath.size() == depth){
							paths.add(path);
						}
					}
				}
			}
			if(paths.size() == 0)
				break;
			int random = RandomUtils.nextInt(0, paths.size() - 1);
			List<Integer> chosenPath = paths.get(random);
			chosenPath = subPath(chosenPath);
			List<Integer> newPath = getImplementation().symbolicExecution(cfg, testInfo, chosenPath);
			testInfo.totalCost = testInfo.totalCost + newPath.size() - 1;
			pathSolved.add(chosenPath);
			
			//remove nodes are visited in this testcase
			removeVisitedNode(newPath);
			
			for(Integer node : newPath){
				if(visited.containsKey(node))
					visited.get(node).replaceArrivePath(newPath);
				else
					visited.put(node, new Node(node, rtPath));
			}
		}
		testInfo.coverageRatio = 1 - (double)unvisited.size() / node_number;
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
    	CoverageOptimizedSearch cos = new CoverageOptimizedSearch(cfg, imple);
		System.out.println(cos.runTest());	
	}

}

class Node {
	int nodeIdx;
	List<Integer> arrivePath = new ArrayList<>();
	
	public Node(int nodeIdx, List<Integer> path){
		this.nodeIdx = nodeIdx;
		List<Integer> newPath = new ArrayList<>();
		for(int i = 0; i < path.size(); i++)
			if(path.get(i) != nodeIdx)
				newPath.add(path.get(i));
			else
				break;
		arrivePath = newPath;
	}
	
	public void replaceArrivePath(List<Integer> path) {
		List<Integer> newPath = new ArrayList<>();
		for(int i = 0; i < path.size(); i++)
			if(path.get(i) != nodeIdx)
				newPath.add(path.get(i));
			else
				break;
		if(arrivePath.size() > newPath.size())
			arrivePath = newPath;
	}
	
	public List<Integer> generateNewPath(List<Integer> path) {
		List<Integer> temp = new ArrayList<>();
		for(Integer i : arrivePath)
			temp.add(i);
		for(Integer i : path)
			temp.add(i);
		return temp;
	}
	
}
