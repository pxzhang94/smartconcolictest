package sunjun.sutd.smartconcolic.algorithm;

import java.util.List;

/**
 * @author wangjingyi
 *
 */
public class MDPTransitionDistribution {
	
	List<Double> transition_probabilities;
	List<MDPState> next_states;
	
	public MDPTransitionDistribution(List<Double> transition_probabilities, List<MDPState> next_states) {
		this.transition_probabilities = transition_probabilities;
		this.next_states = next_states;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((next_states == null) ? 0 : next_states.hashCode());
		result = prime * result + ((transition_probabilities == null) ? 0 : transition_probabilities.hashCode());
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
		MDPTransitionDistribution other = (MDPTransitionDistribution) obj;
		if (next_states == null) {
			if (other.next_states != null)
				return false;
		} else if (!next_states.equals(other.next_states))
			return false;
		if (transition_probabilities == null) {
			if (other.transition_probabilities != null)
				return false;
		} else if (!transition_probabilities.equals(other.transition_probabilities))
			return false;
		return true;
	}

	public List<Double> getTransition_probabilities() {
		return transition_probabilities;
	}

	public List<MDPState> getNext_states() {
		return next_states;
	}
	
}
