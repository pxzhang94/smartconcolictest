package smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjingyi
 *
 */
public class MDPAction {
	
	List<Integer> path; // a path for se
	String type; // rt or se
	
	public MDPAction(List<Integer> path, String type) {
		this.path = path;
		this.type = type;
	}
	
	public MDPAction(String type) {
		this.type = type;
		this.path = new ArrayList<>();
	}
	
	public List<Integer> getPath() {
		return path;
	}

	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		MDPAction other = (MDPAction) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	public String getActionLabel(){
		if(type.equals("rt")){
			return "rt";
		}
		return "se" + pathToString(path);
	}
	
	@Override
	public String toString() {
		return "MDPAction [path=" + path + ", type=" + type + "]";
	}
	
	/**
	 * @param path a testing path
	 * @return the string representation of the path
	 */
	private String pathToString(List<Integer> path){
		String ps = "";
		for(int i : path){
			ps += Integer.toString(i);
		}
		return ps;
	}

}
