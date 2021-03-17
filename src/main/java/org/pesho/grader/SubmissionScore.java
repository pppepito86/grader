package org.pesho.grader;

import java.util.ArrayList;
import java.util.List;

import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;

public class SubmissionScore implements GradeListener {
	
	private boolean finished;
	private StepResult compileResult;
	private List<StepResult> groupResults;
	private List<StepResult> testResults;
	private double score;
	
	public SubmissionScore() {
		this.testResults = new ArrayList<>();
		this.groupResults = new ArrayList<>();
	}
	
	public SubmissionScore(int groupsCount, int testsCount) {
		this.groupResults = new ArrayList<>();
		for (int i = 0; i < groupsCount; i++) this.groupResults.add(new StepResult(Verdict.WAITING));
		this.testResults = new ArrayList<>();
		for (int i = 0; i < groupsCount; i++) this.testResults.add(new StepResult(Verdict.WAITING));
	}
	
	public void setCompileResult(StepResult stepResult) {
		this.compileResult = stepResult;
	}
	
	public void addTestResult(int testNumber, StepResult stepResult) {
		if (testResults.size() < testNumber) testResults.add(stepResult);
		else testResults.set(testNumber-1, stepResult);
	}
	
	public void addGroupResult(int groupNumber, StepResult stepResult) {
		if (groupResults.size() < groupNumber) groupResults.add(stepResult);
		else groupResults.set(groupNumber-1, stepResult);
	}
	
	public void addFinalScore(String verdict, double score) {
		this.score = score;
		this.finished = true;
	}

	public StepResult getCompileResult() {
		return compileResult;
	}
	
	public List<StepResult> getTestResults() {
		return testResults;
	}
	
	public List<StepResult> getGroupResults() {
		return groupResults;
	}
	
	public double getScore() {
		return score;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
}
