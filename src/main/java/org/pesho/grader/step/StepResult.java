package org.pesho.grader.step;

public class StepResult {
	
	private Verdict verdict;
	private String reason;
	
	public StepResult(Verdict verdict) {
		this(verdict, null);
	}

	public StepResult(Verdict verdict, String reason) {
		this.verdict = verdict;
		this.reason = reason;
	}
	
	public Verdict getVerdict() {
		return verdict;
	}
	
	public String getReason() {
		return reason;
	}
	
}
