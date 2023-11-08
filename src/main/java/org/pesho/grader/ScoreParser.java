package org.pesho.grader;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.Precision;
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
		if (score.getCompileResult().getVerdict() == Verdict.CE) return Verdict.CE.toString();
		
		if (details.testsScoring()) {
			return getTestsScore();
		} else {
			return getGroupsScore();
		}
	}
	
	public String getTestsScore() {
		return IntStream.range(0, score.getTestResults().size())
				.mapToObj(i -> {
					Verdict verdict = score.getTestResults().get(i).getVerdict();
					if (verdict == Verdict.WAITING) return "wait";
					if (details.getTestGroups().get(i).getWeight() == 0) return "["+verdict.name()+"]";
					return verdict.name();
				})
				.collect(Collectors.joining(","));
	}
	
	public String getGroupsScore() {
		return score.getGroupResults().stream()
				.map(result -> {
					if (result.getVerdict() == Verdict.WAITING) return "wait";
					
					String points = ""+Precision.round(result.getPoints(), 2);
					if (points.endsWith(".00")) points = points.replace(".00", "");
					if (points.endsWith(".0")) points = points.replace(".0", "");
					if (result.getVerdict() == Verdict.OK) return ""+points;
					if (result.getVerdict() == Verdict.PARTIAL) return "("+points+")";
					
					String verdictName=result.getVerdict().name();
					if (verdictName.equals("SKIPPED")) verdictName="SK";
					return verdictName;
				})
				.collect(Collectors.joining(","));
	}
}
