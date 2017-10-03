package sunjun.sutd.smartconcolic.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.AllDirectedPaths;
import org.jgrapht.graph.DefaultWeightedEdge;

import cfgcoverage.jacoco.analysis.data.CFG;
import cfgcoverage.jacoco.analysis.data.CfgCoverage;
import cfgcoverage.jacoco.analysis.data.CfgNode;
import cfgcoverage.jacoco.analysis.data.NodeCoverage;
import gentest.junit.TestsPrinter.PrintOption;
import jdart.core.JDartCore;
import jdart.core.JDartParams;
import jdart.core.socket2.JDartClient;
import jdart.core.socket2.JDartServerSingle;
import jdart.model.TestInput;
import learntest.core.LearnTestParams;
import learntest.core.LearntestParamsUtils;
import learntest.core.LearntestParamsUtils.GenTestPackage;
import learntest.core.commons.data.classinfo.TargetMethod;
import learntest.core.gentest.GentestParams;
import learntest.core.gentest.GentestResult;
import main.RunJPF;
import sav.common.core.SavException;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.dto.execute.value.ExecVar;
import sunjun.sutd.smartconcolic.algorithm.TestInfo;
import sunjun.sutd.smartconcolic.dataPoint2Sample.DataRunner;
import sunjun.sutd.smartconcolic.dataPoint2Sample.SimpleLearntest;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;

public class RealImplementation implements IImplementation {

	private LearnTestParams params;
	private AppJavaClassPath appClasspath;
	private TargetMethod targetMethod;
	
	private CfgCoverage coverage;
	private Map<List<Integer>, String[]> pathMap = new HashMap<>();
	
	public RealImplementation(LearnTestParams params, AppJavaClassPath appClasspath, TargetMethod targetMethod) {
		super();
		this.params = params;
		this.appClasspath = appClasspath;
		this.targetMethod = targetMethod;
	}

	@Override
	public List<Integer> randomTesting(SmartCFG cfg) {
		GentestParams gentestParams = LearntestParamsUtils.createGentestParams(appClasspath, params, GenTestPackage.INIT);
		gentestParams.setPrintOption(PrintOption.APPEND);
		
		learntest.core.LearnTest learntest = new learntest.core.LearnTest(appClasspath);
		
		GentestResult gentestResult = learntest.randomGentests(gentestParams);
		List<Integer> list = new ArrayList<>();
		try {
			setCoverage(learntest.runCfgCoverage(targetMethod, gentestResult.getJunitClassNames()));
			
			for(CfgNode node: getCoverage().getCfg().getNodeList()){
				NodeCoverage nc = getCoverage().getCoverage(node);
				if(nc.isCovered()){
					list.add(node.getIdx());
				}
			}
			
		} catch (SavException e) {
			e.printStackTrace();
		}
		
		return list;
	}

	@Override
	public List<Integer> symbolicExecution(SmartCFG cfg, TestInfo testInfo, List<Integer> path) {
		//be careful, the = and add method of list is shadow copy
		List<Integer> tempPath = new ArrayList<>();
		for(int i = 0; i < path.size(); i++)
			tempPath.add(path.get(i));
		
		testInfo.se_number++;
		if(pathMap.containsKey(tempPath))
			testInfo.totalCost += Double.valueOf(pathMap.get(tempPath)[0]);
		else
			testInfo.totalCost += 3000.0; //time out
		testInfo.se_paths.add(tempPath);

		List<Integer> newPath = new ArrayList<>();
		if(pathMap.containsKey(tempPath) && pathMap.get(tempPath)[1] != "NULL"){
			SimpleLearntest learntest = new SimpleLearntest(appClasspath);
			try {
				learntest.run(params);
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<double[]> data = new LinkedList<>();
			String[] values = pathMap.get(tempPath)[1].split(",");
			Map<String, Double> valuation = new HashMap<>();
			for(String value : values){
				String[] temp = value.split(":=");
				valuation.put(temp[0], Double.valueOf(temp[1]));
			}
			List<ExecVar> vars = learntest.initProbes.getOriginalVars();
			double[] testData = new double[vars.size()];
			for(int i = 0; i < vars.size(); i++){
				testData[i] = valuation.get(vars.get(i).getVarId());
			}
			data.add(testData);
			List<int[]> returnPath = new DataRunner(learntest).runData(data, vars);
			int current_node = cfg.getInit_node();
			newPath.add(current_node);
			while(!cfg.getTerminate_nodes().contains(current_node)){
				for(int[] node : returnPath){
					if(node[0] == current_node){
						current_node = node[1];
						newPath.add(current_node);
						break;
					}
				}
				current_node++;
				newPath.add(current_node);
			}
		}
		else
			newPath.add(0);
		return newPath;
	}
	
	@Override
	public Map<List<Integer>, String[]> getSymbolicExecutionCost(SmartCFG cfg) {
		JDartParams params = JDartServerSingle.constructJDartParams();
		String[] config = new String[]{
				"+app=" + params.getAppProperties(),
				"+site=" + params.getSiteProperties(),
				"+jpf-jdart.classpath+=" + params.getClasspathStr(),
				"+target=" + params.getMainEntry(),
				"+concolic.method=" + params.getMethodName(),
				"+concolic.method." + params.getMethodName() + "=" +params.getClassName()+"."+ params.getMethodName() + params.getParamString(),
				"+concolic.method." + params.getMethodName() + ".config=all_fields_symbolic",
				"+jdart.tree.dont.print=true", // do not print tree
				"+search.min_free="+params.getMinFree(),
				"+search.timeLimit="+params.getTimeLimit()
		};
		RunJPF jpf = new RunJPF();
		jpf.run(config);
		for(Entry<List<int[]>, String[]> entry : jpf.pathMap.entrySet()){
			List<int[]> tempPath = entry.getKey();
			List<Integer> path = new ArrayList<>();
			int i = -1;
			int node = -1;
			while(i < tempPath.size() - 1){
				node = (i != -1) ? tempPath.get(i)[0] : cfg.getInit_node();
				i++;
				int temp_end_node = tempPath.get(i)[0];
				while(node != temp_end_node){
					path.add(node);
					node = getNextNode(cfg, node, tempPath, i);
				}
			}
			path.add(node);
			i++;
			path.add(getNextNode(cfg, node, tempPath, i));
			String[] tempValue = entry.getValue();
			pathMap.put(path, tempValue);
		}
		return pathMap;
	}

	/**
	 * Get all the connecting paths between two nodes.
	 * 
	 * @param cfg
	 * @param target_node the end node
	 * 
	 * @return all the paths between source_node and target_node
	 */
	public List<List<Integer>> getAllConnectingPaths(SmartCFG cfg, int target_node) {
		int source_node = cfg.getInit_node();
		List<List<Integer>> connecting_paths = new ArrayList<>();
//		AllDirectedPaths<Integer, DefaultWeightedEdge> adp = new AllDirectedPaths<>(prob_graph);
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
	
	public int getNextNode(SmartCFG cfg, int node, List<int[]> tempPath, int i){
		List<Integer> branches = new ArrayList<>();
		double[] branchArray = cfg.getTransition_matrix().getRow(node);
    	for(int j = 0; j < branchArray.length; j++)
    		if(branchArray[j] > 0)
    			branches.add(j);
		if(branches.size() == 1)
			return branches.get(0);
		else
			return branches.get(1 - tempPath.get(i - 1)[1]);
	}
	
	public CfgCoverage getCoverage() {
		return coverage;
	}

	public void setCoverage(CfgCoverage coverage) {
		this.coverage = coverage;
	}

	@Override
	public Map<List<Integer>, String[]> getPathMap() {
		return this.pathMap;
	}

}
