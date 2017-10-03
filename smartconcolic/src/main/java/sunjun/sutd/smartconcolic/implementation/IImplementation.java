package sunjun.sutd.smartconcolic.implementation;

import java.util.List;
import java.util.Map;

import cfgcoverage.jacoco.analysis.data.CFG;
import sunjun.sutd.smartconcolic.algorithm.TestInfo;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;

public interface IImplementation {
	
	/**
	 * The implementation of random testing.
	 * 
	 * @param init_node the begin node
	 * 
	 * @return the path
	 */
	public List<Integer> randomTesting(SmartCFG cfg);
	
	/**
	 * The implementation of symbolic execution.
	 * 
	 * @return the whole path
	 */
	public List<Integer> symbolicExecution(SmartCFG cfg, TestInfo testInfo, List<Integer> path);
	
	/**
	 * Get the minimum cost of each pair between init_node and others.
	 * 
	 * @param cfg
	 * 
	 * @return the Map of path and minimum cost
	 */
	public Map<List<Integer>, String[]> getSymbolicExecutionCost(SmartCFG cfg);
	
	public List<List<Integer>> getAllConnectingPaths(SmartCFG cfg, int target_node);
	
	public Map<List<Integer>, String[]> getPathMap();
	
}
