package org.pesho.grader.step;

import java.util.Optional;

public class StepResult {
	
	private Verdict verdict;
	private String reason;
	private Integer exitCode;
	private String output;
	private String expectedOutput;
	private Double time;
	private Long memory;
	private Double checkerOutput;
	private Double points;
	
	public StepResult() {
	}
	
	public StepResult(Verdict verdict) {
		this.verdict = verdict;
	}
	
	public StepResult(Verdict verdict, String reason) {
		this.verdict = verdict;
		this.reason = reason;
	}
	
	public StepResult(Verdict verdict, String reason, Integer exitCode) {
		this.verdict = verdict;
		this.reason = reason;
		this.exitCode = exitCode;
	}
	
	public StepResult(Verdict verdict, String reason, Integer exitCode, Double time, Long memory) {
		this.verdict = verdict;
		this.reason = reason;
		this.exitCode = exitCode;
		this.time = time;
		this.memory = memory;
	}
	
	public StepResult(Verdict verdict, String reason, Double time, Long memory, Double points) {
		this.verdict = verdict;
		this.reason = reason;
		this.time = time;
		this.memory = memory;
		this.points = points;
	}

	public StepResult(Verdict verdict, String output, String expectedOutput) {
		this.verdict = verdict;
		this.output = output;
		this.expectedOutput = expectedOutput;
	}

	public StepResult(Verdict verdict, String reason, String output, String expectedOutput, double checkerOutput) {
		this.verdict = verdict;
		this.reason = reason;
		this.output = output;
		this.expectedOutput = expectedOutput;
		this.checkerOutput = checkerOutput;
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
	
	public Integer getExitCode() {
		return exitCode;
	}
	
	public void setExitCode(Integer exitCode) {
		this.exitCode = exitCode;
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
	
	public Long getMemory() {
		return memory;
	}
	
	public void setMemory(Long memory) {
		this.memory = memory;
	}

	public Double getCheckerOutput() {
		if (verdict == Verdict.OK) return 1.0;
		return Optional.ofNullable(checkerOutput).orElse(0.0);
	}
	
	public void setPoints(Double points) {
		this.points = points;
	}
	
	public Double getPoints() {
		return points;
	}
	
}
