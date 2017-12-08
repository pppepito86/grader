package org.pesho.grader;

import java.util.LinkedHashMap;

import org.pesho.grader.step.Verdict;

public class SubmissionScore {
	
	private LinkedHashMap<String, Verdict> scoreSteps;
	private double score;
	
	public SubmissionScore() {
		scoreSteps = new LinkedHashMap<>();
	}
	
	public void addScoreStep(String step, Verdict verdict) {
		scoreSteps.put(step, verdict);
		this.score += score;
	}
	
	public void addScore(double score) {
		this.score = score;
	}
	
	public LinkedHashMap<String, Verdict> getScoreSteps() {
		return scoreSteps;
	}
	
	public double getScore() {
		return score;
	}

}
