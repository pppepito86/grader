package org.pesho.grader;

import java.util.ArrayList;
import java.util.List;

import org.pesho.grader.step.StepResult;

public class SubmissionScore implements GradeListener {
	
	private boolean finished;
	private StepResult compileResult;
	private List<StepResult> testResults;
	private List<StepResult> groupResults;
	private double score;
	
	public SubmissionScore() {
		this.testResults = new ArrayList<>();
		this.groupResults = new ArrayList<>();
	}
	
	public void setCompileResult(StepResult stepResult) {
		this.compileResult = stepResult;
	}
	
	public void addTestResult(int testNumber, StepResult stepResult) {
		this.testResults.add(stepResult);
	}
	
	public void addGroupResult(int groupNumber, StepResult stepResult) {
		this.groupResults.add(stepResult);
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
