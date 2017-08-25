package smartconcolic.rmc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.linear.RealMatrix;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class SmartCFG implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8402924356125171698L;
	RealMatrix transition_matrix;
	RealMatrix cost_matrix;
	int non_zero_transitions;
	int[] transitions_per_row;
	int init_node; // assume a unique initial node
	Set<Integer> terminate_nodes;
	Set<Integer> branch_nodes = new HashSet<>();
	Set<Integer> loop_nodes;	
	DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> prob_graph;
	DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> cost_graph;
	
	public SmartCFG(RealMatrix transition_matrix, RealMatrix cost_matrix, int init_node, Set<Integer> terminate_nodes, 
			Set<Integer> loop_nodes) {
		this.transition_matrix = transition_matrix;
		this.cost_matrix = cost_matrix;
		this.transitions_per_row = new int[transition_matrix.getRowDimension()];
		for(int i=0; i<transition_matrix.getRowDimension(); i++){
			double[] row = transition_matrix.getRow(i);
			int tpr = 0;
			for(double d : row){
				if(d>0){
					tpr++;
					non_zero_transitions++;
				}
			}
			transitions_per_row[i] = tpr;
		}
		this.init_node = init_node;
		this.terminate_nodes = terminate_nodes;
		this.loop_nodes = loop_nodes;
		generateBranchNodes();
		generateCostGraph();
		generateProbGraph();
	}
	
	/**
	 * Get all the branch nodes.
	 */
	public void generateBranchNodes() {
		for(int i = 0; i < transition_matrix.getRowDimension(); i++){
			for(int j = 0; j < transition_matrix.getColumnDimension(); j++){
				String prob = String.valueOf(transition_matrix.getEntry(i, j));
				if(!prob.equals("0.0") && !prob.equals("1.0")){
					branch_nodes.add(i);
					break;
				}
			}
		}
	}
	
	/**
	 * Generate cost graph based on cost matrix.
	 */
	public void generateCostGraph() {
		int node_number = transition_matrix.getRowDimension();
		DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = 
				new DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge>
		(DefaultWeightedEdge.class);
		
		for(int i=0; i<node_number; i++){
			graph.addVertex(i);
		}
		
		for(int i=0; i<node_number; i++){
			for(int j=0; j<node_number; j++){
				double cost = cost_matrix.getEntry(i, j);
				if(cost>0){
					DefaultWeightedEdge edge = graph.addEdge(i, j);
					graph.setEdgeWeight(edge, cost);
				}
			}
		}
		cost_graph = graph;
	}
	
	/**
	 * Generate probability graph based on transition matrix.
	 */
	public void generateProbGraph() {
		int node_number = transition_matrix.getRowDimension();
		DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = 
				new DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge>
		(DefaultWeightedEdge.class);
		
		for(int i=0; i<node_number; i++){
			graph.addVertex(i);
		}
		
		for(int i=0; i<node_number; i++){
			for(int j=0; j<node_number; j++){
				double tran_prob = transition_matrix.getEntry(i, j);
				if(tran_prob>0){
					DefaultWeightedEdge edge = graph.addEdge(i, j);
					graph.setEdgeWeight(edge, tran_prob);
				}
			}
		}
		prob_graph = graph;
	}
	
	public int getNon_zero_transitions() {
		return non_zero_transitions;
	}

	public RealMatrix getTransition_matrix() {
		return transition_matrix;
	}

	public RealMatrix getCost_matrix() {
		return cost_matrix;
	}

	public int[] getTransitions_per_row() {
		return transitions_per_row;
	}


	public int getInit_node() {
		return init_node;
	}

	public Set<Integer> getTerminate_nodes() {
		return terminate_nodes;
	}

	public Set<Integer> getLoop_nodes() {
		return loop_nodes;
	}

	public DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> getProb_graph() {
		return prob_graph;
	}

	public void setProb_graph(DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> prob_graph) {
		this.prob_graph = prob_graph;
	}

	public DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> getCost_graph() {
		return cost_graph;
	}

	public void setCost_graph(DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> cost_graph) {
		this.cost_graph = cost_graph;
	}

	public Set<Integer> getBranch_nodes() {
		return branch_nodes;
	}

	public void setBranch_nodes(Set<Integer> branch_nodes) {
		this.branch_nodes = branch_nodes;
	}
	
	@Override
	public String toString() {
		return "RandomCFG [transition_matrix=" + transition_matrix + ", cost_matrix=" + cost_matrix
				+ ", non_zero_transitions=" + non_zero_transitions + ", transitions_per_row="
				+ Arrays.toString(transitions_per_row) + ", init_node=" + init_node + ", terminate_nodes="
				+ terminate_nodes + ", branch_nodes=" + branch_nodes + "]";
	}

	
	
}
