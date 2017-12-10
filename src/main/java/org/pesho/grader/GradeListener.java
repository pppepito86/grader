package org.pesho.grader;

import org.pesho.grader.step.StepResult;

public interface GradeListener {
	
	void addScoreStep(String step, StepResult result);	
	
	void addScore(double score);

}
