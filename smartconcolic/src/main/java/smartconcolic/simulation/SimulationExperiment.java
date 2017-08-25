package smartconcolic.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import smartconcolic.SimulationOptimalTest;
import smartconcolic.algorithm.Algorithm;
import smartconcolic.algorithm.ContextGuidedSearch;
import smartconcolic.algorithm.CoverageOptimizedSearch;
import smartconcolic.algorithm.DepthFirstSearch;
import smartconcolic.algorithm.DirectedSearch;
import smartconcolic.algorithm.GenerationalSearch;
import smartconcolic.algorithm.GreedyAlgorithm;
import smartconcolic.algorithm.GreedyWithKnowledge;
import smartconcolic.algorithm.OptimalCostAlgorithm;
import smartconcolic.algorithm.RandomPathSelection;
import smartconcolic.algorithm.RandomStateSearch;
import smartconcolic.algorithm.RandomTesting;
import smartconcolic.algorithm.SubpathGuidedSearch;
import smartconcolic.implementation.IImplementation;
import smartconcolic.implementation.SimulationImplementation;
import smartconcolic.rmc.RandomCFGFactory;
import smartconcolic.rmc.SmartCFG;
import smartconcolic.utils.FileUtils;

public class SimulationExperiment {
	
	
	String exp_root;
	
	public SimulationExperiment(String exp_root) {
		this.exp_root = exp_root;
	}
	
	public double generateRandomCFG(int node_number, double rare_density, double rare_level, int count) throws IOException{
		String cfg_root = exp_root + "/" + node_number + "-nodes/rd=" + rare_density + "/rl=" + rare_level + "/CFG";
		String mdp_root = exp_root + "/" + node_number + "-nodes/rd=" + rare_density + "/rl=" + rare_level + "/MDP";
		FileUtils.createDir(cfg_root);
		FileUtils.createDir(mdp_root);
		
		double optimal_cost = -1;
		int terminate_point = node_number/5;
		
		RandomCFGFactory fac = new RandomCFGFactory(node_number, terminate_point, 0.2, 0, rare_density, rare_level, 1000);
		while(true){
			SmartCFG cfg = fac.generateRandomCFG();
			OptimalCostAlgorithm oca = new OptimalCostAlgorithm(cfg, new SimulationImplementation());
			oca.buildMDP();
			oca.generatePrismMDPModel(mdp_root, node_number+"-"+count);
			
			String model_path = mdp_root + "/" + node_number+"-"+count;
			String property_path = exp_root + "/mdp_pctl";
			
			optimal_cost = SimulationOptimalTest.getOptimalCost(model_path, property_path);
			System.out.println("optimal cost of generated CFG: " + optimal_cost);
			if(optimal_cost!=-1){
				FileUtils.writeObject(cfg_root + "/" + node_number+"-"+count, cfg);
				break;
			}
		}
		return optimal_cost;
	}
	
	public static double getAlgorithmAverageCost(String algo_type, SmartCFG cfg, int repitition) throws Exception{
		double total_cost = 0;
		Algorithm algo = null;
		IImplementation imp = new SimulationImplementation();
		for(int i=0; i<repitition; i++){
			
			switch (algo_type) {
			case "GAK":
				algo = new GreedyWithKnowledge(cfg, imp);
				break;
			case "GA":
				algo = new GreedyAlgorithm(cfg, imp);
				break;
			case "RT":
				algo = new RandomTesting(cfg, imp);
				break;
			case "RSS":
				algo = new RandomStateSearch(cfg, imp);
				break;
			case "COS":
				algo = new CoverageOptimizedSearch(cfg, imp);
				break;
			case "RPS":
				algo = new RandomPathSelection(cfg, imp);
				break;
			case "DFS":
				algo = new DepthFirstSearch(cfg, imp);
				break;
			case "DART":
				algo = new DirectedSearch(cfg, imp);
				break;
			case "GS":
				algo = new GenerationalSearch(cfg, imp);
				break;
			case "CGS":
				algo = new ContextGuidedSearch(cfg, imp);
				break;
			case "SGS":
				algo = new SubpathGuidedSearch(cfg, imp, cfg.getTransition_matrix().getRowDimension()/5);
				break;
			 default:
	             throw new IllegalArgumentException("Invalid algorithm type: " + algo_type);
			}
			total_cost += getAlgoCost(cfg, algo);
		}
		return total_cost/repitition;
	}
	
	public static double getAlgoCost(SmartCFG cfg, Algorithm algo) throws Exception{
		return algo.runTest().getTotalCost();
	}
	
	public static void main(String[] args) throws Exception{
		SimulationExperiment se = new SimulationExperiment(System.getProperty("user.dir") + "/resources");
		int cfg_number = 20;
		int[] node_number = new int[]{
				5,
				10,
				15,
				20};
		double rare_density = 0.8;
		double rare_level = 1e-4;
		String[] algo_types = new String[]{
//				"GAK", 
//				"GA", 
//				"RSS",
				"COS",
//				"RPS", 
//				"DFS", 
//				"DART", 
//				"GS", 
//				"CGS", 
//				"SGS",
//				"RT", 
				};
		
		// generate a set of CFGs for each node
//		List<List<Double>> optimal_costs = new ArrayList<>();
//		for(int nn : node_number){
//			List<Double> nn_optimal_costs = new ArrayList<>();
//			System.out.println("====== current node number: " + nn + " ======");
//			if(nn!=5)
//				rare_density = 0.2;
//			
//			String cfg_root = se.exp_root + "/" + nn + "-nodes/rd=" + rare_density + "/rl=" + rare_level + "/CFG";
//			String mdp_root = se.exp_root + "/" + nn + "-nodes/rd=" + rare_density + "/rl=" + rare_level + "/MDP";
//			
////			FileUtils.cleanDirectory(cfg_root);
////			FileUtils.cleanDirectory(mdp_root);
//			
//			for(int i=0; i<cfg_number; i++){
//				String model_path = mdp_root + "/" + nn + "-"+i;
//				String property_path = se.exp_root + "/mdp_pctl";
//				System.out.println("--- generating CFG number : " + i);
////				SmartCFG cfg = (SmartCFG) FileUtils.readObject(cfg_root + "/" + node_number+"-"+i);
//				nn_optimal_costs.add(SimulationOptimalTest.getOptimalCost(model_path, property_path));
////				nn_optimal_costs.add(se.generateRandomCFG(nn, rare_density, rare_level, i));
//			}
//			System.out.println("--- node optimal cost : " + nn_optimal_costs);
//			System.out.println("--- node mean optimal cost: " + mean(nn_optimal_costs));
//			optimal_costs.add(nn_optimal_costs);
//		}
//		System.out.println("optimal costs for each CFG: " + optimal_costs);
//		FileUtils.writeObject(se.exp_root+"/optimal_costs", optimal_costs);
		
		
		for(String algo_type : algo_types){
			System.out.println("*************** current algo type: " + algo_type + " *****************");
			rare_density = 0.8;
			// calculate mean cost for each set of cfgs
			List<Double> mean_costs = new ArrayList<>();
			for(int nn : node_number){
				System.out.println("====== current node number: " + nn + " ======");
				if(nn!=5){
					rare_density = 0.2;
				}
				String cfg_root = se.exp_root + "/" + nn + "-nodes/rd=" + rare_density + "/rl=" + rare_level + "/CFG";
				List<Double> algo_cost = new ArrayList<>();
				for(int i=0; i<cfg_number; i++){
					SmartCFG cfg = (SmartCFG) FileUtils.readObject(cfg_root + "/" + nn +"-"+i);
					algo_cost.add(getAlgorithmAverageCost(algo_type, cfg, 1000));
				}
				System.out.println("current algorithm cost : " + algo_cost);
				mean_costs.add(mean(algo_cost));
			}
			System.out.println("current algorithm average costs for " + cfg_number + " cfgs : " + mean_costs);
		}
//		
		
	}
	
	public static double mean(List<Double> ld){
		double total = 0;
		for(double d : ld)
			total += d;
		return total/ld.size();
	}

}
