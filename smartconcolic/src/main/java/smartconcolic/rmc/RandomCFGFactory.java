package smartconcolic.rmc;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import smartconcolic.utils.RandomUtils;

/**
 * @author wangjingyi
 *
 */
public class RandomCFGFactory {

	int node_number;
	int terminate_point; 
	double branch_density;
	int loop_number; 
	double rare_trans_density; 
	double rare_level; 
	int max_se_cost; 


	/**
	 * @param node_number number of node in the CFG of the program
	 * @param terminate_point the number of terminate node in the program
	 * @param branch_density the probability of a node having branches
	 * @param loop_number the number of loops in the program
	 * @param rare_trans_density the probability that a branch has a rare transition
	 * @param rare_level how rare is the transition, e.g. 1e-6
	 * @param max_se_cost maximum symbolic execution cost
	 */
	public RandomCFGFactory(int node_number, int terminate_point, double branch_density, 
			int loop_number, double rare_trans_density, double rare_level, int max_se_cost) {
		this.node_number = node_number;
		this.terminate_point = terminate_point;
		this.branch_density = branch_density;
		this.loop_number = loop_number;
		this.rare_trans_density = rare_trans_density;
		this.rare_level = rare_level;
		this.max_se_cost = max_se_cost;
	}
	
	public boolean isStronglyConnected(SmartCFG cfg){
		Set<Integer> unvisited = new HashSet<>();
		Set<Integer> cared = new HashSet<>();
		for(int i=1; i<node_number; i++)
			unvisited.add(i);
		
		Stack<Integer> visit_node = new Stack<>();
		visit_node.add(0);
		cared.add(0);
		while(!visit_node.isEmpty()){
			int current_visit = visit_node.pop();
			cared.add(current_visit);
			
			double[] dist = cfg.getTransition_matrix().getRow(current_visit);
			for(int i=0; i<dist.length; i++){
				if(dist[i]>0){
					if(!cared.contains(i)){
						visit_node.push(i);
					}
					unvisited.remove(i);
				}
			}
		}
		if(unvisited.size()>0)
			return false;
		
		return true;
				
	}

	/**
	 * @return the randomly generated CFG
	 */
	public SmartCFG generateRandomCFG(){
		RealMatrix transition_matrix = null; 
		RealMatrix cost_matrix = null; 

		if(node_number>50){ // if the transition matrix is large, use sparse matrix implementation
			transition_matrix = new OpenMapRealMatrix(node_number, node_number);
			cost_matrix = new OpenMapRealMatrix(node_number, node_number);
		}
		else{
			transition_matrix = MatrixUtils.createRealMatrix(node_number, node_number);
			cost_matrix = MatrixUtils.createRealMatrix(node_number, node_number);
		}
		
		
		HashSet<Integer> terminate_nodes = pickTerminateNodes(transition_matrix); // randomly pick terminate nodes
		transition_matrix.setEntry(node_number-1, node_number-1, 1); // the last node is enforced to be terminate node

		int loop_node = -1;
//				pickLoopNode(terminate_nodes); // randomly pick a loop node, currently only one loop node

		for(int i=0; i<node_number-1; i++){
			if(terminate_nodes.contains(i)){ // terminate node, self loop with probability 1
				transition_matrix.setEntry(i, i, 1);
				continue;
			}

			if(i==loop_node){ // loop node
				int next_node = RandomUtils.nextInt(0, i-1); // randomly pick a backward node
				double[] random_distribution = RandomUtils.generateRandomDistribution(2); // randomly generate a distribution
				transition_matrix.setEntry(i, next_node, random_distribution[0]);
				transition_matrix.setEntry(i, i+1, random_distribution[1]); // its successor is a next node
			}
			else{
				if(i==node_number-2){ // the second last node doesn't have a branch, otherwise a loop is generated
					transition_matrix.setEntry(i, i+1, 1);
					cost_matrix.setEntry(i, i+1, generateRandomCost());
					continue;
				}
				
				double branch = RandomUtils.nextDouble();
				if(branch<branch_density || terminate_nodes.contains(i+1)){ // tell whether to generate a branch or not
					int next_node = i + 2;
					
					if(!terminate_nodes.contains(i+1))
						next_node = generatePostNode(i); 
					else if(terminate_nodes.contains(i+2) && i+2!=node_number-1) // avoid the two post nodes are all terminate nodes
						terminate_nodes.remove(i+2);
					
					double rare = RandomUtils.nextDouble();
					double[] random_distribution = null;
					if(rare<rare_trans_density){ // tell whether this branch has a rare transition
						random_distribution = RandomUtils.generateRareRandomDistribution(2, rare_level);
					}
					else{
						random_distribution = RandomUtils.generateRandomDistribution(2);
					}
					
					transition_matrix.setEntry(i, i+1, random_distribution[0]);
					transition_matrix.setEntry(i, next_node, random_distribution[1]);
					
					cost_matrix.setEntry(i, i+1, generateRandomCost());
					cost_matrix.setEntry(i, next_node, generateRandomCost());
					
				}
				else{ // not a branch, go to the successor node
					transition_matrix.setEntry(i, i+1, 1);
					cost_matrix.setEntry(i, i+1, generateRandomCost());
				}
			}
		}
		
		// loop nodes
		Set<Integer> loop_nodes = new HashSet<>();
		loop_nodes.add(loop_node);
		
		return new SmartCFG(transition_matrix, cost_matrix, 0, terminate_nodes, loop_nodes);
	}

//	private int pickLoopNode(HashSet<Integer> terminate_nodes) {
//		int loop_node = -1;
//		boolean generate_success = false;
//		while(!generate_success){
//			int j = RandomUtils.nextInt(1, node_number-1);
//			if(!terminate_nodes.contains(j)){
//				loop_node = j;
//				generate_success = true;
//			}
//		}
//		return loop_node;
//	}


	/**
	 * @param transition_matrix
	 * @return the set of terminate nodes
	 */
	private HashSet<Integer> pickTerminateNodes(RealMatrix transition_matrix) {
		HashSet<Integer> terminate_nodes = new HashSet<>();
		for(int i=0; i<terminate_point-1; i++){
			boolean generate_success = false;
			while(!generate_success){
				int j = RandomUtils.nextInt(1, node_number-2); // the first node cannot be terminate node
				if(!terminate_nodes.contains(j)){
					terminate_nodes.add(j);
					generate_success = true;
				}
			}

		}
		terminate_nodes.add(node_number-1); // add last node as terminate node
		return terminate_nodes;
	}
	
	/**
	 * @return a random cost for symbolic execution
	 */
	private int generateRandomCost(){
		return RandomUtils.nextInt(2, max_se_cost);
	}

	/**
	 * @param current_node current node
	 * @return a node after the current node
	 */
	private int generatePostNode(int current_node){
		int j = RandomUtils.nextInt(current_node+2, node_number-1);
		return j;
	}
	
	public static void main(String[] args){
		RandomCFGFactory fac = new RandomCFGFactory(20, 4, 0.5, 0, 0.2, 1e-3, 1000);
		int count = 0;
		for(int i=0; i<100000; i++){
			SmartCFG cfg = fac.generateRandomCFG();
			System.out.println("--- current cfg: " + (i+1));
			if(fac.isStronglyConnected(cfg)){
				count++;
			}
		}
		System.out.println("### strongly connected CFGs: " + count);
	}
	
}
