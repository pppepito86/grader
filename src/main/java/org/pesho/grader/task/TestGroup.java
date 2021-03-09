package org.pesho.grader.task;

import java.util.Arrays;
import java.util.List;

public class TestGroup {

	private double weight;
	private List<TestCase> testCases;
	private boolean hasFeedback;

	public TestGroup() {
	}
	
	public TestGroup(double weight, boolean hasFeedback, TestCase... testCases) {
		this.weight = weight;
		this.hasFeedback = hasFeedback;
		this.testCases = Arrays.asList(testCases);
	}
	
	public double getWeight() {
		return weight;
	}
	
	public boolean hasFeedback() {
		return hasFeedback;
	}

	public List<TestCase> getTestCases() {
		return testCases;
	}
	
	
}