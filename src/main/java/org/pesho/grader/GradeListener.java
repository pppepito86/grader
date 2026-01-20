package org.pesho.grader;

import org.pesho.grader.step.StepResult;

public interface GradeListener {
	
	default void setCompileResult(StepResult compileResult) {}
	default boolean setCompileResultWithBlocking(String submissionId, String type, StepResult compileResult) {
		return true;
	}
	
	default void addTestResult(int testNumber, StepResult testResult) {}
	default boolean addTestResultWithBlocking(String submissionId, String type, int testNumber, StepResult testResult) {
		return true;
	}
	
	default void addGroupResult(int groupNumber, StepResult groupResult) {}
	/*default boolean addGroupResultWithBlocking(String submissionId, String type, int groupNumber, StepResult groupResult) {
		return true;
	}*/
	
	default void addFinalScore(double score, boolean finished) {}
	/*default boolean addScoreWithBlocking(String submissionId, String type, double score, boolean finished) {
		return true;
	}*/

	default void scoreUpdated(String submissionId, SubmissionScore score) {}
	default boolean scoreUpdatedWithBlocking(String submissionId, SubmissionScore score) {
		return true;
	}

}
