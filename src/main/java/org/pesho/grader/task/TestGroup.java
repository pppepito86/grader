package org.pesho.grader.task;

import java.util.Arrays;
import java.util.List;

public class TestGroup {

	private double weight;
	private List<TestCase> testCases;

	public TestGroup() {
	}
	
	public TestGroup(double weight, TestCase... testCases) {
		this.weight = weight;
		this.testCases = Arrays.asList(testCases);
	}
	
	public double getWeight() {
		return weight;
	}
	
	public List<TestCase> getTestCases() {
		return testCases;
	}
	
}