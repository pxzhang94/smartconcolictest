package smartconcolic;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import smartconcolic.algorithm.Algorithm;
import smartconcolic.algorithm.ContextGuidedSearch;
import smartconcolic.algorithm.DepthFirstSearch;
import smartconcolic.algorithm.DirectedSearch;
import smartconcolic.algorithm.GenerationalSearch;
import smartconcolic.algorithm.GreedyAlgorithm;
import smartconcolic.algorithm.RandomPathSelection;
import smartconcolic.algorithm.RandomStateSearch;
import smartconcolic.algorithm.RandomTesting;
import smartconcolic.algorithm.SubpathGuidedSearch;
import smartconcolic.implementation.IImplementation;
import smartconcolic.implementation.SimulationImplementation;
import smartconcolic.rmc.SmartCFG;
import smartconcolic.utils.FileUtils;

public class Main {

	public static void main(String[] args) throws Exception {
		SmartCFG cfg = (SmartCFG)FileUtils.readObject("/Users/pxzhang/Documents/SUTD/smartconcolictest/smartconcolic/resources/0.5/1e-5/CFG/10/10-3");
		System.out.println(cfg.getTransition_matrix());
//		System.out.println(cfg.getCost_matrix());
		
//    	RealMatrix transition_matrix = new OpenMapRealMatrix(8, 8);
//    	RealMatrix cost_matrix = new OpenMapRealMatrix(8, 8);
//    	
//    	transition_matrix.setEntry(0, 1, 0.5);
//    	transition_matrix.setEntry(1, 2, 1);
//    	transition_matrix.setEntry(0, 2, 0.5);
//    	transition_matrix.setEntry(2, 3, 0.5);
//    	transition_matrix.setEntry(2, 4, 0.5);
//    	transition_matrix.setEntry(4, 7, 0.5);
//    	transition_matrix.setEntry(4, 5, 0.5);
//    	transition_matrix.setEntry(5, 7, 0.5);
//    	transition_matrix.setEntry(5, 6, 0.5);
//    	transition_matrix.setEntry(6, 7, 1);
//    	
//    	cost_matrix.setEntry(0, 1, 5);
//    	cost_matrix.setEntry(0, 2, 5);
//    	cost_matrix.setEntry(2, 3, 20);
//    	cost_matrix.setEntry(2, 4, 20);
//    	cost_matrix.setEntry(4, 7, 50);
//    	cost_matrix.setEntry(4, 5, 50);
//    	cost_matrix.setEntry(5, 7, 10);
//    	cost_matrix.setEntry(5, 6, 10);
//    	Set<Integer> ten = new HashSet<Integer>();
//    	Set<Integer> ln = new HashSet<Integer>();
//    	ten.add(Integer.valueOf(3));
//    	ten.add(Integer.valueOf(7));
//    	SmartCFG cfg = new SmartCFG(transition_matrix, cost_matrix, Integer.valueOf(0), ten, ln);   	
//    	IImplementation imple = new SimulationImplementation();
//    	
//    	System.out.println("Greedy");
//		Algorithm algo1 = new GreedyAlgorithm(cfg, imple);
//		algo1.runTest();
//		
//		System.out.println("RT");
//		Algorithm algo2 = new RandomTesting(cfg, imple);
//		algo2.runTest();
//		
//		System.out.println("RSS");
//		Algorithm algo3 = new RandomStateSearch(cfg, imple);
//		algo3.runTest();
//		
//		System.out.println("RPS");
//		Algorithm algo4 = new RandomPathSelection(cfg, imple);
//		algo4.runTest();
//		
//		System.out.println("DFS");
//		Algorithm algo5 = new DepthFirstSearch(cfg, imple);
//		algo5.runTest();
//		
//		System.out.println("DS");
//		Algorithm algo6 = new DirectedSearch(cfg, imple);
//		algo6.runTest();
//		
//		System.out.println("GS");
//		Algorithm algo7 = new GenerationalSearch(cfg, imple);
//		algo7.runTest();
//    	
//		System.out.println("CGS");
//		Algorithm algo8 = new ContextGuidedSearch(cfg, imple);
//		algo8.runTest();
//    	
//		System.out.println("SGS");
//		Algorithm algo9 = new SubpathGuidedSearch(cfg, imple, 1);
//		algo9.runTest();
	}

}
