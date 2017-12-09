package org.pesho.grader;

import java.util.LinkedHashMap;

import org.pesho.grader.step.StepResult;

public class SubmissionScore {
	
	private LinkedHashMap<String, StepResult> scoreSteps;
	private double score;
	
	public SubmissionScore() {
		scoreSteps = new LinkedHashMap<>();
	}
	
	public void addScoreStep(String step, StepResult result) {
		scoreSteps.put(step, result);
	}
	
	public void addScore(double score) {
		this.score = score;
	}
	
	public LinkedHashMap<String, StepResult> getScoreSteps() {
		return scoreSteps;
	}
	
	public double getScore() {
		return score;
	}

}
