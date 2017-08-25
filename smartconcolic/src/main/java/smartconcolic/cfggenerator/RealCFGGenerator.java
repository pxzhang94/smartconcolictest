package smartconcolic.cfggenerator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import cfgcoverage.jacoco.analysis.data.CFG;
import cfgcoverage.jacoco.analysis.data.CfgNode;
import smartconcolic.rmc.SmartCFG;

public class RealCFGGenerator implements ICFGGenerator {

	private SmartCFG smartCFG;
	
	public RealCFGGenerator(CFG cfg) {
		int size = cfg.getNodeList().size();
		RealMatrix transitionMatrix = new OpenMapRealMatrix(size, size);
		for(CfgNode node: cfg.getNodeList()){
			int pre = node.getIdx();
			if(null != node.getBranches()){
				for(CfgNode branchNode: node.getBranches()){
					int post = branchNode.getIdx();
					transitionMatrix.setEntry(pre, post, 1.0 / node.getBranches().size());
				}				
			}
		}
		Set<Integer> terminate_node = new HashSet<>();
		for(int i = 0; i < transitionMatrix.getRowDimension(); i++){
			for(int j = 0; j < transitionMatrix.getColumnDimension(); j++){
				if(transitionMatrix.getEntry(i, j) > 0)
					break;
				if(j == transitionMatrix.getColumnDimension() - 1)
					terminate_node.add(i);
			}
		}
			
		RealMatrix costMatrix = new OpenMapRealMatrix(size, size);
		
		smartCFG = new SmartCFG(transitionMatrix, costMatrix, cfg.getStartNode().getIdx(), terminate_node, null);
	}

	@Override
	public SmartCFG getCFG() {
		return this.smartCFG;
	}

}
