package sunjun.sutd.smartconcolic.algorithm;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import sunjun.sutd.smartconcolic.SimulationOptimalTest;
import sunjun.sutd.smartconcolic.implementation.IImplementation;
import sunjun.sutd.smartconcolic.implementation.SimulationImplementation;
import sunjun.sutd.smartconcolic.rmc.SmartCFG;
import sunjun.sutd.smartconcolic.utils.FileUtils;

/**
 * @author wangjingyi
 *
 */
public class OptimalCostAlgorithm extends Algorithm {

	List<MDPAction> possible_actions = new ArrayList<>();
	List<List<Integer>> possible_random_paths = new ArrayList<>();
	Set<MDPState> created_states = new HashSet<>(); // stores states that are already created
	Set<MDPState> cared_states = new HashSet<>(); // stores states that are already looked at 
	int state_number;

	public OptimalCostAlgorithm(SmartCFG cfg, IImplementation imple){
		super(cfg, imple);
		this.node_number = cfg.getTransition_matrix().getRowDimension();
		this.testInfo = new TestInfo();
		for(int i = 0; i < node_number; i++)
			unvisited.add(i);
		getPossibleActions();
		this.possible_random_paths = getPossibleRandomPaths(cfg.getInit_node());
	}

	
	/**
	 * identify all the possible states, actions and transitions of the MDP
	 */
	public void buildMDP(){

		MDPState init_state = new MDPState(state_number++, unvisited);
		created_states.add(init_state);
		Stack<MDPState> uncared_state = new Stack<>();
		uncared_state.push(init_state);

		while(!uncared_state.isEmpty()){

			MDPState current_state = uncared_state.pop();
//			System.out.println("- current state under care: " + current_state);
			cared_states.add(current_state);
			current_state.setActions(possible_actions);
			

			for(MDPAction action : possible_actions){
				MDPTransitionDistribution trans_dist = getTransitionDistribution(current_state, action);
				current_state.getTransition_distributions().add(trans_dist);
			}

			// update uncared states
			for(MDPTransitionDistribution trans_dist : current_state.getTransition_distributions()){
				List<MDPState> next_states = trans_dist.getNext_states();
				for(MDPState next_state : next_states){
					if(!uncared_state.contains(next_state) // avoid duplicate adding 
							&& !cared_states.contains(next_state)){ // avoid adding states already taken care of
						uncared_state.push(next_state);
					}
				}
			}
		}
	}
	
	

	/**
	 * get the set of actions
	 */
	private void getPossibleActions(){

		possible_actions.add(new MDPAction("rt")); // add random testing

		int init_node = cfg.getInit_node();
		for(int i=1; i<node_number; i++){
//			List<List<Integer>> connecting_paths = getAllConnectingPaths(init_node, i);
			List<List<Integer>> connecting_paths = ((SimulationImplementation)getImplementation()).getAllConnectingPaths(cfg, i);
			for(List<Integer> path : connecting_paths){
				possible_actions.add(new MDPAction(path, "se")); // get possible symbolic execution actions
			}
		}
	}


	/**
	 * @param current_state the state 
	 * @param action the action
	 * @return the probability distribution given the state and the action
	 */
	private MDPTransitionDistribution getTransitionDistribution(MDPState current_state, MDPAction action){

		Set<Integer> uncovered = current_state.getUncovered();

		List<Double> transition_probabilities = new ArrayList<>();
		List<MDPState> next_states = new ArrayList<>();
		
		if(uncovered.size()==0){ // all covered self loop
			transition_probabilities.add(1.0);
			next_states.add(current_state);
			return new MDPTransitionDistribution(transition_probabilities, next_states);
		}

		if(action.getType().equals("rt")){ // random testing
			
			double sum_prob = 0;
			for(List<Integer> path : possible_random_paths){
				double trans_prob = getPathProbability(path);
				sum_prob += trans_prob;
				transition_probabilities.add(trans_prob);

				Set<Integer> new_uncovered = removeVisited(cloneSet(uncovered), path);
				MDPState state = getCorrespondingMDPState(new_uncovered,created_states);
				if(state==null){ // create a new state
					MDPState new_state = new MDPState(state_number++, new_uncovered);
					next_states.add(new_state);
					created_states.add(new_state);
//					System.out.println("--- adding new state : " + new_state);
				}
				else{
					next_states.add(state);
				}
			}
			assert 1- sum_prob<0.000000001 : "=== sumed probability not equal to 1 ===";
			return new MDPTransitionDistribution(transition_probabilities, next_states);
		}

		// symbolic execution cases
		List<Integer> se_path = action.getPath();
		int se_init_node = se_path.get(se_path.size()-1); // last element
		List<List<Integer>> possible_se_paths = getPossibleRandomPaths(se_init_node);

		for(List<Integer> path : possible_se_paths){

			double trans_prob = getPathProbability(path);
			transition_probabilities.add(trans_prob);

			Set<Integer> new_uncovered = removeVisited(cloneSet(uncovered), se_path);
			new_uncovered = removeVisited(new_uncovered, path);

			MDPState state = getCorrespondingMDPState(new_uncovered, created_states);
			if(state==null){ // create a new state
				MDPState new_state = new MDPState(state_number++, new_uncovered);
				next_states.add(new_state);
				created_states.add(new_state);
//				System.out.println("--- adding new state : " + new_state);
			}
			else{
				next_states.add(state);
			}
		}
		return new MDPTransitionDistribution(transition_probabilities, next_states);

	}

	/**
	 * @param uncovered the set of uncovered nodes
	 * @param states the set of MDP states
	 * @return the state with the same set of uncovered nodes
	 */
	private MDPState getCorrespondingMDPState(Set<Integer> uncovered, Set<MDPState> states){
		for(MDPState state : states){
			if(state.getUncovered().equals(uncovered)){
				return state;
			}
		}
		return null;
	}

	/**
	 * @param orig the set of integers to clone
	 * @return a deep copy of the set of integers
	 */
	private Set<Integer> cloneSet(Set<Integer> orig){
		Set<Integer> copy = new HashSet<>();
		copy.addAll(orig);
		return copy;
	}

	/**
	 * @param uncovered the set of nodes that are currently uncovered
	 * @param path the new testing path
	 * @return the set of nodes that are uncovered after obtaining the new path
	 */
	private Set<Integer> removeVisited(Set<Integer> uncovered, List<Integer> path){
		for(int i : path){
			if(uncovered.contains(i)){
				uncovered.remove(i);
			}
		}
		return uncovered;
	}


	/**
	 * @param init_node the initial node to start testing
	 * @return the set of paths from the initial node to the terminate nodes
	 */
	private List<List<Integer>> getPossibleRandomPaths(int init_node){

		List<List<Integer>> possible_random_paths = new ArrayList<>();

		Set<Integer> terminate_nodes = cfg.getTerminate_nodes();

		for(int i : terminate_nodes){
//			possible_random_paths.addAll(getAllConnectingPaths(init_node, i));
			possible_random_paths.addAll(((SimulationImplementation)getImplementation()).getAllConnectingPaths(cfg, i));
		}
		return possible_random_paths;
	}

	/**
	 * @param file_path the path to the genereated PRISM MDP file
	 * @param file_name the name of the MDP file
	 * @throws FileNotFoundException
	 */
	public void generatePrismMDPModel(String file_path, String file_name) throws FileNotFoundException{

		String mdp_model = "mdp\n\n";
		mdp_model += "module translatedMDP\n\n";

		mdp_model += "s : [0.." + (state_number-1) + "] init 0;\n\n";

		for(MDPState state : cared_states){
			mdp_model += getStateTransitionRows(state);
			mdp_model += "\n";
		}

		mdp_model += "endmodule\n\n";

		// rewards 
		mdp_model += "rewards\n";
		mdp_model += getRewards();
		mdp_model += "endrewards\n\n";

		// label for covered
		mdp_model += getLabel();
		
		FileUtils.writeStringToFile(file_path+"/"+file_name, mdp_model);
	}

	/**
	 * @param state current state
	 * @return the string in PRISM language that contains all the possible actions and transition distributions under each action
	 */
	private String getStateTransitionRows(MDPState state){
		String rows = "";
		int action_size = state.getActions().size();
		for(int i=0; i<action_size; i++){
			MDPAction action = state.getActions().get(i);
			MDPTransitionDistribution trans_dist = state.getTransition_distributions().get(i);
			rows += "[" + action.getActionLabel() + "] s=" + state.getState_id() + " -> ";

			int dist_size = trans_dist.getNext_states().size();
			
			if(dist_size==0 || dist_size==1){ // self loop state
				rows += "1 :(s'="+state.getState_id()+");\n";
				continue;
			}
			
			assert trans_dist.getNext_states().size()==trans_dist.getTransition_probabilities().size() : "=== not a valid distribution ===";
			for(int j=0; j<dist_size-1; j++){
				rows += trans_dist.getTransition_probabilities().get(j) + ":(s'="+trans_dist.getNext_states().get(j).getState_id()+")+";
			}
			rows += trans_dist.getTransition_probabilities().get(dist_size-1) + ":(s'="+trans_dist.getNext_states().get(dist_size-1).getState_id()+");\n";
		}
		return rows;
	}

	/**
	 * @return the string in PRISM language of the label that all nodes are covered
	 */
	private String getLabel() {
		
		int covered_id = -1;
		for(MDPState state : cared_states){
			if(state.getUncovered().size()==0){
				covered_id = state.getState_id();
//				System.out.println("--- target state: " + covered_id);
				break;
			}
		}
		
		String label = "label \"covered\" = s = " + covered_id + ";";
		return label;
	}

	/**
	 * @return the string in PRISM language of the rewards for each action
	 */
	private String getRewards() {
		String rewards = "";
		for(MDPAction action : possible_actions){
			if(action.getType().equals("rt")){
				rewards += "[rt] true : 1;\n";
			}
			else{
//				rewards += "[" + action.getActionLabel() + "] true : " + getPathSymbolicExecutionCost(action.getPath()) + ";\n";
				rewards += "[" + action.getActionLabel() + "] true : " + ((SimulationImplementation)getImplementation()).getPathSymbolicExecutionCost(cfg, action.getPath()) + ";\n";
			}
		}
		return rewards;
	}
	
}
