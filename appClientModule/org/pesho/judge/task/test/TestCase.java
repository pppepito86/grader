package org.pesho.judge.task.test;

public class TestCase {

	//private int number;
	private String input;
	private String output;
	
	public TestCase() {
	}
	
	public TestCase(String input, String output) {
		this.input = input;
		this.output = output;
	}
	
	public String getInput() {
		return input;
	}
	
	public String getOutput() {
		return output;
	}
	
}

/*

{
	"points" : 100,
	"groups" : [
		{
			"wight":0.5,
			"tests":["test01.in, test01.out",""]
		
		},
		{},
		{}
	]


}




*/