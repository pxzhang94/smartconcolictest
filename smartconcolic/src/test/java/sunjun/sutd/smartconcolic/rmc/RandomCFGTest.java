package sunjun.sutd.smartconcolic.rmc;

import org.junit.Test;

import sunjun.sutd.smartconcolic.algorithm.OptimalCostAlgorithm;
import sunjun.sutd.smartconcolic.implementation.SimulationImplementation;

public class RandomCFGTest {
	
	
	@Test
	public void testCFGGeneration(){
		RandomCFGFactory fac = new RandomCFGFactory(10, 2, 0.5, 1, 0.4, 1e-5, 100);
		SmartCFG cfg = fac.generateRandomCFG();
		
		System.out.println("generated cfg: " + cfg);
		
		SimulationImplementation impl = new SimulationImplementation();
		OptimalCostAlgorithm oc = new OptimalCostAlgorithm(cfg, impl);
		System.out.println("connecting path: " + impl.getAllConnectingPaths(cfg, 9));
	}
}
