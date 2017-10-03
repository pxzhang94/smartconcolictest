package sunjun.sutd.smartconcolic.algorithm;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;

import sunjun.sutd.smartconcolic.implementation.SimulationImplementation;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;

public class OptimalCostTest {
	
	@Test
	public void testOptimalCost() throws FileNotFoundException{
		
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
    	
    	OptimalCostAlgorithm oca = new OptimalCostAlgorithm(cfg, new SimulationImplementation());
    	oca.buildMDP();
    	oca.generatePrismMDPModel(System.getProperty("user.home"), "optimal_test_test");
		
	}

}
