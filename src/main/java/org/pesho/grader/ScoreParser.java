package org.pesho.grader;

import java.util.stream.Collectors;

import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.grader.task.TaskDetails;

public class ScoreParser {

	private SubmissionScore score;
	private TaskDetails details;
	
	public ScoreParser(SubmissionScore submissionScore, TaskDetails taskDetails) {
		this.score = submissionScore;
		this.details = taskDetails;
	}

	public String getVerdict() {
		if (score.getCompileResult() == null) return "";
		
		if (score.getTestResults().isEmpty()) return score.getCompileResult().getVerdict().toString();
		
		if (details.getWeights().isEmpty()) {
			return getTestsScore();
		} else {
			return getGroupsScore();
		}
	}
	
	public String getTestsScore() {
		return score.getTestResults().stream()
				.map(StepResult::getVerdict)
				.map(Verdict::name)
				.collect(Collectors.joining(","));
	}

	public String getGroupsScore() {
		return score.getGroupResults().stream()
				.map(result -> {
					long points = Math.round(result.getPoints());
					if (result.getVerdict() == Verdict.PARTIAL) return "("+points+")";
					return ""+points;
				})
				.collect(Collectors.joining(","));
	}
}
