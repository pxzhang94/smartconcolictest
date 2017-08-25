package smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author wangjingyi
 *
 */
public class MDPState {
	
	int state_id;
	Set<Integer> uncovered; // nodes that are uncovered
	List<MDPAction> actions;
	List<MDPTransitionDistribution> transition_distributions;
	
	public MDPState(Set<Integer> uncovered) {
		this.uncovered = uncovered;
		this.actions = new ArrayList<>();
		this.transition_distributions = new ArrayList<>();
	}
	
	public MDPState(int state_id, Set<Integer> uncovered) {
		this.state_id = state_id;
		this.uncovered = uncovered;
		this.actions = new ArrayList<>();
		this.transition_distributions = new ArrayList<>();
	}
	

	public MDPState(Set<Integer> uncovered, List<MDPAction> actions, List<MDPTransitionDistribution> transition_distributions) {
		this.uncovered = uncovered;
		this.actions = actions;
		this.transition_distributions = transition_distributions;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uncovered == null) ? 0 : uncovered.hashCode()); // only check if the uncovered nodes are equal
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MDPState other = (MDPState) obj;
		if (uncovered == null) { // only check if the uncovered nodes are equal
			if (other.uncovered != null)
				return false;
		} else if (!uncovered.equals(other.uncovered))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "MDPState [state_id=" + state_id + ", uncovered=" + uncovered + ", actions=" + actions
				+ ", transition_distributions=" + transition_distributions + "]";
	}
	
	public String getPrismLable(){
		return "s" + state_id;
	}


	public Set<Integer> getUncovered() {
		return uncovered;
	}


	public void setUncovered(Set<Integer> uncovered) {
		this.uncovered = uncovered;
	}


	public List<MDPAction> getActions() {
		return actions;
	}


	public void setActions(List<MDPAction> actions) {
		this.actions = actions;
	}


	public List<MDPTransitionDistribution> getTransition_distributions() {
		return transition_distributions;
	}


	public void setTransition_distributions(List<MDPTransitionDistribution> transition_distributions) {
		this.transition_distributions = transition_distributions;
	}
	
	public int getState_id() {
		return state_id;
	}

	public void setState_id(int state_id) {
		this.state_id = state_id;
	}

	

}
