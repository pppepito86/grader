package org.pesho.grader.task;

import java.util.Arrays;
import java.util.List;

public class TaskTests {

	private double points;
	private String checker;
	private List<TestGroup> testGroups;
	
	public TaskTests(double points, String checker, TestGroup... testGroups) {
		this.points = points;
		this.checker = checker;
		this.testGroups = Arrays.asList(testGroups);
	}
	
	public double getPoints() {
		return points;
	}
	
	public String getChecker() {
		return checker;
	}
	
	public List<TestGroup> getTestGroups() {
		return testGroups;
	}
	
}