package org.pesho.grader.step;

public interface BaseStep {
	
	void execute();
	
	Verdict getVerdict();
	
	StepResult getResult();

}
