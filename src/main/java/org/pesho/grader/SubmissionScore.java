package org.pesho.grader;

import java.util.LinkedHashMap;

import org.pesho.grader.step.StepResult;

public class SubmissionScore implements GradeListener {
	
	private LinkedHashMap<String, StepResult> scoreSteps;
	private double score;
	
	public SubmissionScore() {
		scoreSteps = new LinkedHashMap<>();
	}
	
	public void addScoreStep(String step, StepResult result) {
		scoreSteps.put(step, result);
	}
	
	public void addFinalScore(String verdict, double score) {
		this.score = score;
	}
	
	public LinkedHashMap<String, StepResult> getScoreSteps() {
		return scoreSteps;
	}
	
	public double getScore() {
		return score;
	}

}
