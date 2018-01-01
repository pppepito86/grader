package org.pesho.grader.step;

public class StepResult {
	
	private Verdict verdict;
	private String reason;
	
	public StepResult() {
	}
	
	public StepResult(Verdict verdict) {
		this(verdict, null);
	}

	public StepResult(Verdict verdict, String reason) {
		this.verdict = verdict;
		this.reason = reason;
	}
	
	public void setVerdict(Verdict verdict) {
		this.verdict = verdict;
	}
	
	public Verdict getVerdict() {
		return verdict;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public String getReason() {
		return reason;
	}
	
}
