package smartconcolic.rmc;

import org.junit.Test;

import smartconcolic.algorithm.OptimalCostAlgorithm;
import smartconcolic.implementation.SimulationImplementation;
import smartconcolic.rmc.RandomCFGFactory;
import smartconcolic.rmc.SmartCFG;

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
