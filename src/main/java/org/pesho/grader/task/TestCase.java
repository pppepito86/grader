package org.pesho.grader.task;

public class TestCase {

	private int number;
	private String input;
	private String output;
	
	public TestCase() {
	}
	
	public TestCase(int number, String input, String output) {
		this.number = number;
		this.input = input;
		this.output = output;
	}
	
	public int getNumber() {
		return number;
	}
	
	public String getInput() {
		return input;
	}
	
	public void setInput(String input) {
		this.input = input;
	}
	
	public String getOutput() {
		return output;
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
}
