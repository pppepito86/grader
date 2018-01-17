package org.pesho.grader.step;

public class StepResult {
	
	private Verdict verdict;
	private Reason reason;
	
	public StepResult() {
	}
	
	public StepResult(Verdict verdict) {
		this.verdict = verdict;
	}
	
	public StepResult(Verdict verdict, String reason) {
		this.verdict = verdict;
		this.reason = new DefaultReason(reason);
	}
	
	public StepResult(Verdict verdict, Reason reason) {
		this.verdict = verdict;
		this.reason = reason;
	}
	
	public void setVerdict(Verdict verdict) {
		this.verdict = verdict;
	}
	
	public Verdict getVerdict() {
		return verdict;
	}
	
	public void setReason(Reason reason) {
		this.reason = reason;
	}
	
	public Reason getReason() {
		return reason;
	}
	
}
