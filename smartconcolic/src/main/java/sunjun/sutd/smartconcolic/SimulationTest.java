package sunjun.sutd.smartconcolic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import sunjun.sutd.smartconcolic.algorithm.Algorithm;
import sunjun.sutd.smartconcolic.algorithm.ContextGuidedSearch;
import sunjun.sutd.smartconcolic.algorithm.DepthFirstSearch;
import sunjun.sutd.smartconcolic.algorithm.DirectedSearch;
import sunjun.sutd.smartconcolic.algorithm.GenerationalSearch;
import sunjun.sutd.smartconcolic.algorithm.GreedyAlgorithm;
import sunjun.sutd.smartconcolic.algorithm.GreedyWithKnowledge;
import sunjun.sutd.smartconcolic.algorithm.RandomPathSelection;
import sunjun.sutd.smartconcolic.algorithm.RandomStateSearch;
import sunjun.sutd.smartconcolic.algorithm.RandomTesting;
import sunjun.sutd.smartconcolic.algorithm.SubpathGuidedSearch;
import sunjun.sutd.smartconcolic.implementation.IImplementation;
import sunjun.sutd.smartconcolic.implementation.SimulationImplementation;
import sunjun.sutd.smartconcolic.rmc.RandomCFGFactory;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;
import sunjun.sutd.smartconcolic.utils.FileUtils;

public class SimulationTest {

	static String[] algorithm = {"GreedyAlgorithm", "RandomTesting", 
			"RandomStateSearch", "RandomPathSelection", "DepthFirstSearch", 
			"DirectedSearch", "GenerationalSearch", "ContextGuidedSearch", "SubpathGuidedSearch", "GreedyWithKnowledge"};
	
	public static void generateTestCFG(String filePath, int node_number, int terminate_point, double branch_density, 
			int loop_number, double rare_trans_density, double rare_level, int max_se_cost) throws Exception {
		RandomCFGFactory cfgFactory = new RandomCFGFactory(node_number, terminate_point, branch_density, loop_number, rare_trans_density, rare_level, max_se_cost);
		SmartCFG cfg = cfgFactory.generateRandomCFG();
		FileUtils.writeObject(filePath, cfg);
		int index = filePath.lastIndexOf("/");
		String parameterPath = filePath.substring(0, index + 1);
		String parameter = "node number:" + node_number + " terminate point:" + terminate_point 
				+ " branch density:" + branch_density + " loop number:" + loop_number + " rare trans density:" + rare_trans_density
				+ " rare level:" + rare_level + " max se cost:" + max_se_cost;
		FileUtils.writeStringToFile(parameterPath + node_number, parameter);
	}
	
	public static void dataAnalysis(String inPath) throws Exception {
		String outPath = inPath + "result";
		File inFile = new File(inPath);
		File[] fileList1 = inFile.listFiles();
		String[] str = {"", "", "", ""};
		double[][] resultArray = new double[algorithm.length][str.length];
		for(int i = 0; i < algorithm.length; i++){
			for(File file : fileList1){
				if(file.getPath().contains(algorithm[i])){
					File[] fileList2 = file.listFiles();
					for(File tempFile : fileList2){
						if(tempFile.isDirectory()){
							File[] tempFileList = tempFile.listFiles();
							double[] mean = new double[tempFileList.length];
							double[] standardDeviation = new double[tempFileList.length];
							String endResult = "";
							int count = 0;
							for(int j = 0; j < tempFileList.length; j++){
									String[] results = FileUtils.readString(tempFileList[j].getPath()).split("/n");
									count = results.length;
									double[] cost = new double[count]; 
									for(int k = 0; k < count; k++){
										cost[k] = Double.valueOf(results[k].split(":")[4].replace("]", ""));
										mean[j] += cost[k];
									}									
									mean[j] /= count;
									
									for(int k = 0; k < cost.length; k++){
										standardDeviation[j] += Math.pow(cost[k] - mean[j], 2);
									}	
									standardDeviation[j] = Math.pow(standardDeviation[j] / (count - 1), 0.5);
//									standardDeviation[j] = Math.pow(standardDeviation[j] / count, 0.5);
									
									endResult = endResult + "[" + algorithm[i] + "-" + tempFileList[j].getName() + ": " + 
																mean[j] + ", " + standardDeviation[j] + "],";
							}
							double[] totalResults = calculateTotalSD(count, mean, standardDeviation);
							int index = Integer.valueOf(tempFile.getName()) / 5 - 1;
							str[index] = str[index] + algorithm[i] + "-" + tempFile.getName() + ": " + totalResults[0] + ", " + totalResults[1] + "\n" + endResult + "\n\n";
							resultArray[i][index] = totalResults[0];
						}	
					}
				}
			}
		}		
		long time = System.currentTimeMillis();
		for(int i = 0; i < str.length; i++){
			int node_number = 5 * (i + 1);
			FileUtils.appendStringToFile(outPath + node_number + "-" + time, str[i]);
		}
		FileUtils.writeObject(inPath + "Average-" + time, resultArray);
	}
	
	private static double[] calculateTotalSD(int count, double[] mean, double[] standardDeviation) {
		double[] results = new double[2];
		int number = count;
		double totalSD = standardDeviation[0];
		double totalMean = mean[0];
		for(int i = 1; i < mean.length; i++){
			totalSD = Math.pow(((number - 1) * Math.pow(totalSD, 2) + (count - 1) * Math.pow(standardDeviation[i] , 2) + number * count / (number + count) * (Math.pow(totalMean, 2) + Math.pow(mean[i], 2) - 2 * totalMean *mean[i])) / (number + count - 1), 0.5);
//			totalSD = Math.pow((number * Math.pow(totalSD, 2) + count * Math.pow(standardDeviation[i] , 2) + number * count / (number + count) * (Math.pow(totalMean, 2) + Math.pow(mean[i], 2) - 2 * totalMean *mean[i])) / (number + count), 0.5);
			totalMean = (totalMean * number + mean[i] * count) / (number + count);
			number += count;
		}
		results[0] = totalMean;
		results[1] = totalSD;
		return results;
	}

	public static void main(String[] args) throws Exception {
		String path = args[1];		
		
		if(Integer.valueOf(args[0]) == 0){
			path = path + args[6] + "/" + args[7] + "/CFG/";
			FileUtils.createDir(path);
			int node_number = Integer.valueOf(args[2]);
			path = path + node_number + '/';
			FileUtils.createDir(path);
			for(int i = 0; i < 20; i++)
				generateTestCFG(path + node_number + "-" + i, node_number, Integer.valueOf(args[3]), Double.valueOf(args[4]),
						Integer.valueOf(args[5]), Double.valueOf(args[6]), Double.valueOf(args[7]), Integer.valueOf(args[8]));
			System.out.println("Generating CFGs of " + node_number + " nodes " + "is complete.");
		}
		else if(Integer.valueOf(args[0]) == -1){
			dataAnalysis(path);
			System.out.println("Data analysis is complete.");
		}
		else{
			File file = new File(path);
			File[] fileList = file.listFiles();
			Algorithm algo = null;
			for(int j = 0; j < fileList.length; j++){
				if(fileList[j].isDirectory()){
					File[] tempFileList = fileList[j].listFiles();
					for(File tempFile : tempFileList) {
						if(tempFile.getName().contains("-")){
							System.out.println("Running for CFG " + tempFile.getName());
							SmartCFG cfg = (SmartCFG)FileUtils.readObject(tempFile.getPath());
							IImplementation imple = new SimulationImplementation();
							String result = "";
			
//							for(int i = 0; i < 1000; i++){
//								algo = new GreedyAlgorithm(cfg, imple);
//								result = result + algo.runTest() + "\n";
//							}
//							addResultsToFile(tempFile, algo, result);	
//							
//							for(int i = 0; i < 1000; i++){
//								algo = new RandomTesting(cfg, imple);
//								result = result + algo.runTest() + "\n";
//							}
//							addResultsToFile(tempFile, algo, result);	
//								
//							for(int i = 0; i < 1000; i++){
//								algo = new RandomStateSearch(cfg, imple);
//								result = result + algo.runTest() + "\n";
//							}
//							addResultsToFile(tempFile, algo, result);	
//							
//							for(int i = 0; i < 1000; i++){
//								algo = new RandomPathSelection(cfg, imple);
//								result = result + algo.runTest() + "\n";
//							}
//							addResultsToFile(tempFile, algo, result);	
//							
//							for(int i = 0; i < 1000; i++){
//								algo = new DepthFirstSearch(cfg, imple);
//								result = result + algo.runTest() + "\n";
//							}
//							addResultsToFile(tempFile, algo, result);	
//								
//							for(int i = 0; i < 1000; i++){
//								algo = new DirectedSearch(cfg, imple);
//								result = result + algo.runTest() + "\n";
//							}
//							addResultsToFile(tempFile, algo, result);	
//								
//							for(int i = 0; i < 1000; i++){
//								algo = new GenerationalSearch(cfg, imple);
//								result = result + algo.runTest() + "\n";
//							}
//							addResultsToFile(tempFile, algo, result);	
//								
//							for(int i = 0; i < 1000; i++){
//								algo = new ContextGuidedSearch(cfg, imple);
//								result = result + algo.runTest() + "\n";
//							}
//							addResultsToFile(tempFile, algo, result);	
//								
//							for(int i = 0; i < 1000; i++){
//								algo = new SubpathGuidedSearch(cfg, imple, 1);
//								result = result + algo.runTest() + "\n";
//							}
//							addResultsToFile(tempFile, algo, result);
//							
							for(int i = 0; i < 1000; i++){
								algo = new GreedyWithKnowledge(cfg, imple);
								result = result + algo.runTest() + "\n";
							}
							addResultsToFile(tempFile, algo, result);
						}
					}
				}
			}
		}
	}

	private static void addResultsToFile(File tempFile, Algorithm algo, String result) throws FileNotFoundException {
		String[] pathSplit = tempFile.getPath().split("/");
		String tempPath = tempFile.getPath();
		int index = tempPath.indexOf("CFG/");
		tempPath = tempPath.substring(0, index) + "Result/"; 
		FileUtils.createDir(tempPath);
		String outPath = tempPath + algo.getClass().getSimpleName() + "/";
		FileUtils.createDir(outPath);
		outPath = outPath + pathSplit[pathSplit.length - 2] + "/";
		FileUtils.createDir(outPath);
		
		FileUtils.writeStringToFile(outPath + pathSplit[pathSplit.length - 1], result);
		System.out.println("Running " +algo.getClass().getSimpleName() + " is complete.");
	}
}
