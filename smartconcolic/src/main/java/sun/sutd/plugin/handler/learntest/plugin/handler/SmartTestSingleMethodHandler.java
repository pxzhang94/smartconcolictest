package sun.sutd.plugin.handler.learntest.plugin.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import cfgcoverage.jacoco.CfgJaCoCoConfigs;
import cfgcoverage.jacoco.analysis.data.CFG;
import learntest.core.LearnTestParams;
import learntest.core.commons.data.LearnTestApproach;
import learntest.core.commons.data.classinfo.TargetClass;
import learntest.core.commons.data.classinfo.TargetMethod;
import learntest.plugin.LearnTestConfig;
import learntest.plugin.handler.AbstractLearntestHandler;
import learntest.plugin.utils.IProjectUtils;
import learntest.plugin.utils.LearnTestUtil;
import sav.common.core.Constants;
import sav.common.core.ModuleEnum;
import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.common.core.SystemVariables;
import sav.common.core.utils.CollectionUtils;
import sav.strategies.dto.AppJavaClassPath;
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
import sunjun.sutd.smartconcolic.cfggenerator.RealCFGGenerator;
import sunjun.sutd.smartconcolic.implementation.RealImplementation;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;

public class SmartTestSingleMethodHandler extends AbstractLearntestHandler {

	private AppJavaClassPath appClasspath;
	
	@Override
	protected IStatus execute(IProgressMonitor monitor) {
		LearnTestParams params = initLearntestParamsFromPreference();
		appClasspath = initAppJavaClassPath();
		
		LearnTestConfig config = LearnTestConfig.getINSTANCE();
		TargetMethod targetMethod;
		try {
			targetMethod = initTargetMethod(config);
			params.setTargetMethod(targetMethod);
			
			RealImplementation implementation = new RealImplementation(params, appClasspath, targetMethod);
			implementation.randomTesting(null);
			CFG cfg = implementation.getCoverage().getCfg();

			RealCFGGenerator generator = new RealCFGGenerator(cfg);
			SmartCFG scfg = generator.getCFG();
			/*pxzhang*/
			Map<List<Integer>, String[]> pathMap = implementation.getSymbolicExecutionCost(scfg);
	        for (Entry<List<Integer>, String[]> entry : pathMap.entrySet()) {  
	            System.out.println("Key = " + entry.getKey() + ", Cost = " + ((String[])entry.getValue())[0] + ", Value = " + ((String[])entry.getValue())[1]);  
	        }
			
//	        Algorithm algo = new DirectedSearch(scfg, implementation);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}	
//	        Algorithm algo = new RandomTesting(scfg, implementation);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//	        Algorithm algo = new RandomStateSearch(scfg, implementation);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//	        Algorithm algo = new RandomPathSelection(scfg, implementation);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//	        Algorithm algo = new SubpathGuidedSearch(scfg, implementation,1);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//	        Algorithm algo = new GenerationalSearch(scfg, implementation);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//	        Algorithm algo = new GreedyAlgorithm(scfg, implementation);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//	        Algorithm algo = new ContextGuidedSearch(scfg, implementation);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}	        
//	        Algorithm algo = new DepthFirstSearch(scfg, implementation);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//	        Algorithm algo = new GreedyWithKnowledge(scfg, implementation);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			/*pxzhang*/
//			Algorithm algo = new RandomTesting(generator.getCFG(), implementation);
//			try {
//				System.out.println(algo.runTest());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (SavException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	protected String getJobName() {
		return "smart";
	}

	public AppJavaClassPath getAppClasspath() {
		if (appClasspath == null) {
			appClasspath = initAppJavaClassPath();
		}
		return appClasspath;
	}
	
	private AppJavaClassPath initAppJavaClassPath() {
		try {
			IProject project = IProjectUtils.getProject(LearnTestConfig.getINSTANCE().getProjectName());
			IJavaProject javaProject = IProjectUtils.getJavaProject(project);
			AppJavaClassPath appClasspath = new AppJavaClassPath();
			appClasspath.setJavaHome(IProjectUtils.getJavaHome(javaProject));
			appClasspath.addClasspaths(LearnTestUtil.getPrjectClasspath());
			String outputPath = LearnTestUtil.getOutputPath();
			appClasspath.setTarget(outputPath);
			appClasspath.setTestTarget(outputPath);
			appClasspath.setTestSrc(LearnTestUtil.retrieveTestSourceFolder());
			appClasspath.getPreferences().set(SystemVariables.PROJECT_CLASSLOADER, LearnTestUtil.getPrjClassLoader());
			appClasspath.getPreferences().set(SystemVariables.TESTCASE_TIMEOUT,
					Constants.DEFAULT_JUNIT_TESTCASE_TIMEOUT);
			appClasspath.getPreferences().set(CfgJaCoCoConfigs.DUPLICATE_FILTER, true);
			return appClasspath;
		} catch (CoreException ex) {
			throw new SavRtException(ex);
		}
	}
	
	protected LearnTestParams initLearntestParams(LearnTestConfig config) {
		try {
			LearnTestParams params = new LearnTestParams();
			params.setApproach(config.isL2TApproach() ? LearnTestApproach.L2T : LearnTestApproach.RANDOOP);
			try {
				TargetMethod targetMethod = initTargetMethod(config);
				params.setTargetMethod(targetMethod);
			} catch (JavaModelException e) {
				throw new SavException(e, ModuleEnum.UNSPECIFIED, e.getMessage());
			}
			setSystemConfig(params);
			return params;
		} catch (SavException e) {
			throw new SavRtException(e);
		}
	}
	
	private static TargetMethod initTargetMethod(LearnTestConfig config) throws SavException, JavaModelException {
		TargetClass targetClass = new TargetClass(config.getTargetClassName());
		TargetMethod method = new TargetMethod(targetClass);
		method.setMethodName(config.getTargetMethodName());
		method.setLineNum( config.getMethodLineNumber());
		MethodDeclaration methodDeclaration = LearnTestUtil.findSpecificMethod(method.getClassName(), method.getMethodName(), method.getLineNum());
		method.setMethodSignature(LearnTestUtil.getMethodSignature(methodDeclaration));
		List<String> paramNames = new ArrayList<String>(CollectionUtils.getSize(methodDeclaration.parameters()));
		List<String> paramTypes = new ArrayList<String>(paramNames.size());
		for(Object obj: methodDeclaration.parameters()){
			if(obj instanceof SingleVariableDeclaration){
				SingleVariableDeclaration svd = (SingleVariableDeclaration)obj;
				paramNames.add(svd.getName().getIdentifier());
				paramTypes.add(svd.getType().toString());
			}
		}
		method.setParams(paramNames);
		method.setParamTypes(paramTypes);
		return method;
	}

	
}
