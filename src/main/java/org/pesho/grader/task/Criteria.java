package org.pesho.grader.task;

public class Criteria {
	
	private String reason;
	private Double score;
	private Double step;
	private Integer indent;
	
	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public Double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	public Double getStep() {
		return step;
	}
	
	public void setStep(double step) {
		this.step = step;
	}
	
	public Integer getIndent() {
		return indent;
	}
	
	public void setIndent(Integer indent) {
		this.indent = indent;
	}
	
}
