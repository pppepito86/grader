package org.pesho.grader;

import org.pesho.grader.step.StepResult;

public interface GradeListener {
	
	void addScoreStep(String step, StepResult result);	
	
	void addFinalScore(String verdict, double score);

	void scoreUpdated(String submissionId, SubmissionScore score);

}
