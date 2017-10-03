package sunjun.sutd.smartconcolic.algorithm;

import java.util.ArrayList;
import java.util.List;

public class TestInfo {
	
	public int rm_number;  //the number of random testing the test use
	public int se_number;  //the number of symbolic execution the test use
	public List<List<Integer>> se_paths = new ArrayList<>();  //all the path of symbolic execution the test use
	public double totalCost;  //the total cost of the test
	public double coverageRatio;
	
	public int getRMNumber() {
		return rm_number;
	}
	
	public int getSENumber() {
		return se_number;
	}
	
	public List<List<Integer>> getSEPaths() {
		return se_paths;
	}
	
	public double getTotalCost() {
		return totalCost;
	}
	
	public void setRMNumber(int number) {
		this.rm_number = number;
	}
	
	public void setSENumber(int number) {
		this.se_number = number;
	}
	
	public void setSEPaths(List<List<Integer>> paths) {
		this.se_paths = paths;
	}
	
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}
	
	@Override
	public String toString() {
		return "TestInfo [random testing number:" + rm_number + ", symbolic execution number:" + se_number + ", symbolic paths:" +se_paths.toString() +
				", total cost:" + totalCost + ", coverage ratio:" + coverageRatio + "]";
	}
}
