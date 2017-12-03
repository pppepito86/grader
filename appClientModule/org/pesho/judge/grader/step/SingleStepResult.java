package org.pesho.judge.grader.step;

public class SingleStepResult implements StepResult {
	
	private double score;
	
	public SingleStepResult(double score) {
		this.score = score;
	}
	
	@Override
	public double getScore() {
		return score;
	}

}
