package sunjun.sutd.smartconcolic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sunjun.sutd.smartconcolic.algorithm.OptimalCostAlgorithm;
import sunjun.sutd.smartconcolic.implementation.SimulationImplementation;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;
import sunjun.sutd.smartconcolic.utils.FileUtils;

public class SimulationOptimalTest {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException{

		int[] node_numbers = new int[]{20};
		String rare_density = "0.2";
		String rare_level = "1e-4";
		String config_string = rare_density + "/" + rare_level;

		String root_string = System.getProperty("user.dir") + "/resources/" + config_string;

		

		int cfg_each_node = 20;

		double[] cost = new double[]{3431.7,	4500,	8359,-1,	2407.37,	22.68,	-1,	-1,
				-1.0, 291.6496812734587, 7697.54066060191, -1.0, -1.0, -1.0, -1.0, 64.29079259345846, 1081.8538589562957, 59.610652064329756, 219.09068078305467, 348.98235703649544, 7025.616304658026};
				

		System.out.println("average cost : "  + getAverageCost(cost));


//		List<List<Double>> optimal_costs = new ArrayList<>();
//		File result_file = new File(root_string+"/MDP/optimal_cost_results");
//		if(result_file.exists()){
//			optimal_costs = (List<List<Double>>) FileUtils.readObject(root_string+"/MDP/optimal_cost_results");
//			for(List<Double> result : optimal_costs){
//				System.out.println(result);
//			}
//			System.exit(0);
//		}
//
//		for(int node_number : node_numbers){
//			List<Double> costs = new ArrayList<>();
//			String mdp_path = root_string+"/MDP/"+node_number;
//			FileUtils.createDir(mdp_path);
//			for(int i=7; i<20; i++){
//				System.out.println("--- current mdp: " + i);
//				String model_path = mdp_path + "/" + node_number + "-" + i;
//				File mdp_file = new File(model_path);
//				if(!mdp_file.exists()){
//					SmartCFG cfg = (SmartCFG) FileUtils.readObject(root_string+"/CFG/"+node_number+"/"+node_number+"-"+i);
//					OptimalCostAlgorithm alg = new OptimalCostAlgorithm(cfg, new SimulationImplementation());
//					alg.buildMDP();
//					alg.generatePrismMDPModel(mdp_path, node_number+"-"+i);
//				}
//				String property_path = System.getProperty("user.dir")+"/resources/mdp_pctl";
//				costs.add(getOptimalCost(model_path, property_path));
//			}
//			optimal_costs.add(costs);
//			System.out.println("node number: " + node_number + ", optimal cost: " + costs);
//		}
//		FileUtils.writeObject(root_string+"/MDP/optimal_cost_results", optimal_costs);

	}

	/**
	 * @param model_path path to the PRISM model
	 * @param property_path path to the PRISM property
	 * @return the PRISM result of optimal cost
	 */
	public static double getOptimalCost(String model_path, String property_path){

		String[] prism_command = new String[]{System.getProperty("user.home") +"/prism-4.3-osx64/bin/prism", model_path, property_path}; 
		double result = ShellInteraction.extractResultFromCommandOutput(ShellInteraction.executeCommand(prism_command));
		return result;
	}

	public static double getAverageCost(double[] cost){
		double sum_cost = 0;
		int count = 0;
		for(double d : cost){
			if(d!=-1){
				sum_cost += d;
				count ++;
			}
		}
		System.out.println("cannot calculate count : " + (cost.length-count));
		return sum_cost/count;
	}

}
