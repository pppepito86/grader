package org.pesho.grader.step;

public class WAReason implements Reason {

	private String output;
	private String solution;
	
	public WAReason() {
	}
	
	public WAReason(String output, String solution) {
		this.output = output;
		this.solution = solution;
	}

	public String getOutput() {
		return output;
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
	
	public String getSolution() {
		return solution;
	}
	
	public void setSolution(String solution) {
		this.solution = solution;
	}

}
