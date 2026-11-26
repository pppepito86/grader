package org.pesho.grader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.Precision;
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
		if (score.getCompileResult() == null) return "judging";
		if (score.getCompileResult().getVerdict() == Verdict.CE || score.getCompileResult().getVerdict() == Verdict.SE) return score.getCompileResult().getVerdict().toString();
		
		if (score.getType().equals("user_tests") || details.testsScoring()) {
			if (score.getTestResults().size() == 0) {
				if (score.getType().equals("user_tests") && score.isFinished()) return "OK";
				else return "judging";
			}
			return getTestsScore();
		} else {
			if (details.getTestGroups().size() == 0) return "judging";
			return getGroupsScore();
		}
	}
	
	public String getTestsScore() {
		return IntStream.range(0, score.getTestResults().size())
				.mapToObj(i -> {
					Verdict verdict = score.getTestResults().get(i).getVerdict();
					if (verdict == Verdict.WAITING) return "wait";
					if (verdict == Verdict.PARTIAL) {
						String points = "" + Precision.round(score.getTestResults().get(i).getPoints(), 6);
						if (points.contains(".")) points = points.replaceAll("0*$","").replaceAll("\\.$","");
						if (score.getType().equals("submission") && details.getTestGroups().get(i).getWeight() == 0) return "[|"+zeroPoints(score.getTestResults().get(i).getCheckerOutput())+"|]";
						return "|"+points+"|";
					}
					if (score.getType().equals("submission") && details.getTestGroups().get(i).getWeight() == 0) return "["+verdict.name()+"]";
					return verdict.name();
				})
				.collect(Collectors.joining(","));
	}
	
	public String getGroupsScore() {
		return IntStream.range(0, score.getGroupResults().size())
				.mapToObj(i -> {
					StepResult result = score.getGroupResults().get(i);
					if (result.getVerdict() == Verdict.WAITING) return "wait";

					String points = "" + Precision.round(result.getPoints(), 6);
					if (points.contains(".")) points = points.replaceAll("0*$","").replaceAll("\\.$","");
					if (result.getVerdict() == Verdict.OK) return ""+points;
					if (result.getVerdict() == Verdict.PARTIAL) {
						if (i < details.getTestGroups().size() && details.getTestGroups().get(i).getWeight() == 0) return "("+zeroPoints(result.getCheckerOutput())+")";
						return "("+points+")";
					}
					
					String verdictName=result.getVerdict().name();
					if (verdictName.equals("SKIPPED")) verdictName="SK";
					return verdictName;
				})
				.collect(Collectors.joining(","));
	}

	public static final int ZERO_POINTS_CHECKER_OUTPUT_SHIFT = 7;

	private static String zeroPoints (Double checkerOutput) {
		if (checkerOutput == null) return "0";
		return new BigDecimal(checkerOutput).setScale(6, RoundingMode.HALF_UP).movePointLeft(ZERO_POINTS_CHECKER_OUTPUT_SHIFT).stripTrailingZeros().toPlainString();
	}
}
