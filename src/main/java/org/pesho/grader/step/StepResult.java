package org.pesho.grader.step;

public class StepResult {
	
	private Verdict verdict;
	private String reason;
	private String output;
	private String expectedOutput;
	private Double time;
	private Double checkerOutput;
	
	public StepResult() {
	}
	
	public StepResult(Verdict verdict) {
		this(verdict, null);
	}
	
	public StepResult(Verdict verdict, String reason) {
		this.verdict = verdict;
		this.reason = reason;
	}
	
	public StepResult(Verdict verdict, String reason, Double time) {
		this.verdict = verdict;
		this.reason = reason;
		this.time = time;
	}
	
	public StepResult(Verdict verdict, String output, String expectedOutput) {
		this.verdict = verdict;
		this.output = output;
		this.expectedOutput = expectedOutput;
	}

	public StepResult(Verdict verdict, String output, String expectedOutput, double checkerOutput) {
		this.verdict = verdict;
		this.output = output;
		this.expectedOutput = expectedOutput;
		this.checkerOutput = checkerOutput;
	}

	public void setVerdict(Verdict verdict) {
		this.verdict = verdict;
	}
	
	public Verdict getVerdict() {
		return verdict;
	}

	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public String getOutput() {
		return output;
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
	
	public String getExpectedOutput() {
		return expectedOutput;
	}
	
	public void setExpectedOutput(String expectedOutput) {
		this.expectedOutput = expectedOutput;
	}
	
	public Double getTime() {
		return time;
	}
	
	public void setTime(Double time) {
		this.time = time;
	}

	public Double getCheckerOutput() {
		return checkerOutput;
	}
}
