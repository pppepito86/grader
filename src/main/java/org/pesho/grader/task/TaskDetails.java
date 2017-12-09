package org.pesho.grader.task;

import java.util.Arrays;
import java.util.List;

public class TaskDetails {

	private double points;
	private String checker;
	private List<TestGroup> testGroups;
	
	public static TaskDetails create(TaskParser taskParser) {
		TestCase[] testCases = new TestCase[taskParser.testsCount()];
		for (int i = 0; i < testCases.length; i++) {
			testCases[i] = new TestCase(i+1, taskParser.getInput().get(i).getAbsolutePath(), taskParser.getOutput().get(i).getAbsolutePath());
		}
		TestGroup[] testGroups = new TestGroup[testCases.length];
		for (int i = 0; i < testGroups.length; i++) {
			testGroups[i] = new TestGroup(1.0/testCases.length, testCases[i]);
		}
		return new TaskDetails(100, taskParser.getChecker().getAbsolutePath(), testGroups);
	}
	
	public TaskDetails(double points, String checker, TestGroup... testGroups) {
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