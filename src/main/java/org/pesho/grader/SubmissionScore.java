package org.pesho.grader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.math3.util.Precision;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.grader.task.TaskDetails;
import org.pesho.grader.task.TestCase;
import org.pesho.grader.task.TestGroup;

public class SubmissionScore implements GradeListener {

	private boolean finished;
	private StepResult compileResult;
	private List<StepResult> groupResults;
	private List<StepResult> testResults;
	private double score;
	
	// DO NOT REMOVE for backward compatibility
	private LinkedHashMap<String, StepResult> scoreSteps;
	
	public SubmissionScore() {
		this.testResults = new ArrayList<>();
		this.groupResults = new ArrayList<>();
	}
	
	public void setCompileResult(StepResult stepResult) {
		this.compileResult = stepResult;
	}

	public void startingTests(int groupsCount, int testsCount) {
		for (int i = 0; i < groupsCount; i++) this.groupResults.add(new StepResult(Verdict.WAITING));
		for (int i = 0; i < testsCount; i++) this.testResults.add(new StepResult(Verdict.WAITING));
	}
	
	public void addTestResult(int testNumber, StepResult stepResult) {
		if (testResults.size() < testNumber) testResults.add(stepResult);
		else testResults.set(testNumber-1, stepResult);
	}
	
	public void addGroupResult(int groupNumber, StepResult stepResult) {
		if (groupResults.size() < groupNumber) groupResults.add(stepResult);
		else groupResults.set(groupNumber-1, stepResult);
	}
	
	public void addFinalScore(String verdict, double score) {
		this.score = score;
		this.finished = true;
	}

	public StepResult getCompileResult() {
		return compileResult;
	}
	
	public List<StepResult> getTestResults() {
		return testResults;
	}
	
	public List<StepResult> getGroupResults() {
		return groupResults;
	}
	
	public double getScore() {
		return score;
	}
	
	public Double findTime() {
		Double time=null;
		for (StepResult r : groupResults) {
			Double rTime=r.getTime();
			if (rTime==null) continue;
			if ((time==null)||(rTime < 0)) time=rTime;
			else time=Math.max(time, rTime);
			if (time<0) break;
		}
		return time;
	}
	
	public Long findMemory() {
		Long memory=null;
		for (StepResult r : groupResults) {
			Long rMemory=r.getMemory();
			if (rMemory==null) continue;
			if ((memory==null)||(rMemory < 0)) memory=rMemory;
			else memory=Math.max(memory, rMemory);
			if (memory<0) break;
		}
		return memory;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public LinkedHashMap<String, StepResult> getScoreSteps() {
		if (scoreSteps != null) return scoreSteps;
		
		LinkedHashMap<String, StepResult> scoreSteps = new LinkedHashMap<>();
		if (compileResult != null) scoreSteps.put("Compile", compileResult);
		for (int i = 0; i < testResults.size(); i++) scoreSteps.put("Test"+(i+1), testResults.get(i));
		return scoreSteps;
	}

	public double calculateScore (TaskDetails task) {
		double testsScore = 0.0;
		if (compileResult ==  null) setCompileResult(getScoreSteps().get("Compile"));
		if (compileResult.getVerdict() != Verdict.CE) {
			groupResults = new ArrayList<>();
			for (int i = 0; i < task.getTestGroups().size(); i++) {
				TestGroup testGroup = task.getTestGroups().get(i);
				double checkerSum = 0.0;

				Verdict groupVerdict = Verdict.OK;
				Double groupTime = null;
				Long groupMemory = null;
				Integer testInError = null;

				double dependencyScore = 1;
				for (int dependencyGroup: task.dependsOn(i+1)) {
					StepResult dependencyResult = groupResults.get(dependencyGroup-1);
					if (dependencyResult.getVerdict() == Verdict.PARTIAL && dependencyResult.getCheckerOutput() !=  null) {
						dependencyScore = Math.min(dependencyScore, dependencyResult.getCheckerOutput());
					}
				}
				double checkerMin = testGroup.getTestCases().size() != 0 ? dependencyScore : 0.0;
			
				for (int j = 0; j < testGroup.getTestCases().size(); j++) {
					TestCase testCase = testGroup.getTestCases().get(j);
					int testNumber = testCase.getNumber();
					if (testNumber-1 >= testResults.size()) addTestResult(testNumber, scoreSteps.get("Test"+testCase.getNumber())); /// backward compatability
					StepResult result = testResults.get(testNumber-1);
					
					checkerMin = Math.min(checkerMin, result.getCheckerOutput());
					checkerSum += result.getCheckerOutput();
				
					Double time=result.getTime();
					if ((time != null) && ((groupTime == null) || (groupTime >= 0))) {
						if ((groupTime == null) || (time < 0)) groupTime = time;
						else groupTime = Math.max(groupTime, time);
					}
					Long memory=result.getMemory();
					if ((memory != null) && ((groupMemory == null) || (groupMemory >= 0))) {
						if ((groupMemory == null) || (memory < 0)) groupMemory = memory;
						else groupMemory = Math.max(groupMemory, memory);
					}
				
					if (groupVerdict != Verdict.OK && groupVerdict != Verdict.PARTIAL && testInError == null) {
						testInError = j+1;
					}
					if (groupVerdict == Verdict.OK) {
						groupVerdict = result.getVerdict();
					} else if (groupVerdict == Verdict.PARTIAL && result.getVerdict() != Verdict.OK) {
						groupVerdict = result.getVerdict();
					}
				}

				double groupScore = 0;
				if (task.getPoints() == -1) {
					groupScore = checkerSum;
				} else if (task.testsScoring() || task.sumScoring()){
					groupScore = testGroup.getWeight() * checkerSum / testGroup.getTestCases().size();
					if (task.sumScoring() && groupVerdict != Verdict.OK && Double.compare(groupScore, 0.0) != 0) groupVerdict = Verdict.PARTIAL;
				} else {
					groupScore = testGroup.getWeight() * checkerMin;
				}
				testsScore += groupScore;
			
				addGroupResult(i+1, new StepResult(groupVerdict, ""+testInError, groupTime, groupMemory, groupScore*task.getPoints(), checkerMin));
			}
		}

		double finalScore = 0;
		if (task.getPoints() == -1) {
			finalScore = Precision.round(testsScore, task.getPrecision());
		} else {
			finalScore = Precision.round(testsScore * task.getPoints(), task.getPrecision());
		}
		addFinalScore("", finalScore);

		return finalScore;
	}
}
