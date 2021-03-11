package org.pesho.grader;

import org.pesho.grader.step.StepResult;

public interface GradeListener {
	
	void setCompileResult(StepResult compileResult);	
	
	void addTestResult(int testNumber, StepResult testResult);	
	
	void addGroupResult(int groupNumber, StepResult groupResult);
	
	void addFinalScore(String verdict, double score);

	default void scoreUpdated(String submissionId, SubmissionScore score) {}

}
