package org.pesho.grader.task;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class TaskDetails {

	private double points;
	private double time;
	private int memory;
	private String checker;
	private String feedback;
	private String groups;
	private List<TestGroup> testGroups;
	
	public static TaskDetails create(TaskParser taskParser) {
		return new TaskDetails(taskParser);
	}
	
	public TaskDetails() {
	}
	
	public TaskDetails(Properties props, String checker, TestGroup... testGroups) {
        this.points = Double.valueOf(props.getProperty("points", "100"));
        this.time = Double.valueOf(props.getProperty("time", "1"));
        this.memory = Integer.valueOf(props.getProperty("memory", "256"));
        this.feedback = props.getProperty("feedback", "FULL").trim();
        this.groups = props.getProperty("groups", "").trim();
        this.checker = checker;
	}
	
	public TaskDetails(TaskParser taskParser) {
		Properties props = new Properties();
		if (taskParser.getProperties().exists()) {
			try (FileInputStream fileInputStream = new FileInputStream(taskParser.getProperties())) {
				props.load(fileInputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.points = Double.valueOf(props.getProperty("points", "100"));
		this.time = Double.valueOf(props.getProperty("time", "1"));
		this.memory = Integer.valueOf(props.getProperty("memory", "256"));
		this.feedback = props.getProperty("feedback", "FULL").trim();
		this.groups = props.getProperty("groups", "").trim();
		this.checker = taskParser.getChecker().getAbsolutePath();
		
		TestCase[] testCases = new TestCase[taskParser.testsCount()];
		for (int i = 0; i < testCases.length; i++) {
			testCases[i] = new TestCase(i+1, taskParser.getInput().get(i).getAbsolutePath(), taskParser.getOutput().get(i).getAbsolutePath());
		}
		TestGroup[] testGroups = null;
		if (groups.isEmpty()) {
			testGroups = new TestGroup[testCases.length];
			for (int i = 0; i < testGroups.length; i++) {
				testGroups[i] = new TestGroup(1.0/testCases.length, testCases[i]);
			}
		} else {
			String[] split = groups.split(",");
			testGroups = new TestGroup[split.length];
			for (int i = 0; i < testGroups.length; i++) {
				String[] s = split[i].trim().split("-");
				int first = Integer.valueOf(s[0]);
				int last = Integer.valueOf(s[1]);
				TestCase[] cases = new TestCase[last-first+1];
				for (int j = first; j <= last; j++) {
					cases[j-first] = testCases[j-1];
				}
				
				testGroups[i] = new TestGroup(1.0/testGroups.length, cases);
			}
		}
		
		this.testGroups = Arrays.asList(testGroups);
	}

	public void setPoints(double points) {
		this.points = points;
	}
	
	public double getPoints() {
		return points;
	}

	public void setTime(double time) {
		this.time = time;
	}
	
	public double getTime() {
		return time;
	}
	
	public void setMemory(int memory) {
		this.memory = memory;
	}
	
	public int getMemory() {
		return memory;
	}
	
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	
	public String getFeedback() {
		return feedback;
	}
	
	public void setGroups(String groups) {
		this.groups = groups;
	}
	
	public String getGroups() {
		return groups;
	}
	
	public void setChecker(String checker) {
		this.checker = checker;
	}
	
	public String getChecker() {
		return checker;
	}
	
	public void setTestGroups(List<TestGroup> testGroups) {
		this.testGroups = testGroups;
	}
	
	public List<TestGroup> getTestGroups() {
		return testGroups;
	}
	
}