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
	private String type;
	
	// DO NOT REMOVE for backward compatibility
	private LinkedHashMap<String, StepResult> scoreSteps;

	public SubmissionScore() {
		this.testResults = new ArrayList<>();
		this.groupResults = new ArrayList<>();
	}

	public SubmissionScore(String type) {
		this.testResults = new ArrayList<>();
		this.groupResults = new ArrayList<>();
		this.type = type;
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
	
	public void addFinalScore(double score, boolean finished) {
		this.score = score;
		this.finished = finished;
	}

	public void setType (String type) {
		this.type = type;
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

	public String getType() {
		return type;
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
		/*if (scoreSteps != null) return scoreSteps;
		
		LinkedHashMap<String, StepResult> scoreSteps = new LinkedHashMap<>();
		if (compileResult != null) scoreSteps.put("Compile", compileResult);
		for (int i = 0; i < testResults.size(); i++) scoreSteps.put("Test"+(i+1), testResults.get(i));*/
		return scoreSteps;
	}

	public double calculateGroupScore (int groupIndex, double taskPoints, TaskDetails task) {
        TestGroup testGroup = task.getTestGroups().get(groupIndex);
        double checkerSum = 0.0;

        Verdict groupVerdict = Verdict.OK;
        Double groupTime = null;
        Long groupMemory = null;
        Integer testInError = null;

        double dependencyScore = 1;
        for (int dependencyGroup: task.dependsOn(groupIndex+1)) {
			if (dependencyGroup < 1 || dependencyGroup >= groupIndex+1) {
				for (int i = 0; i < testGroup.getTestCases().size(); i++) {
					TestCase testCase = testGroup.getTestCases().get(i);
					int testNumber = testCase.getNumber() + (task.testsFromZero() ? 1 : 0);
					if (testNumber-1 >= testResults.size()) {
						if (scoreSteps != null) addTestResult(testNumber, scoreSteps.get("Test"+(testNumber + (task.testsFromZero() ? -1 : 0)))); /// backward compatability
						else addTestResult(testNumber, new StepResult(Verdict.WAITING));
					}
				}
				addGroupResult(groupIndex+1, new StepResult(Verdict.SE, "", groupTime, groupMemory, 0.0, 0.0));
				return 0;
			}
            StepResult dependencyResult = groupResults.get(dependencyGroup-1);
            if (dependencyResult.getVerdict() == Verdict.PARTIAL && dependencyResult.getCheckerOutput() !=  null) {
                dependencyScore = Math.min(dependencyScore, dependencyResult.getCheckerOutput());
            }
        }
        double checkerMin = testGroup.getTestCases().size() != 0 ? dependencyScore : 0.0;

        for (int i = 0; i < testGroup.getTestCases().size(); i++) {
            TestCase testCase = testGroup.getTestCases().get(i);
            int testNumber = testCase.getNumber() + (task.testsFromZero() ? 1 : 0);
            if (testNumber-1 >= testResults.size()) {
				if (scoreSteps != null) addTestResult(testNumber, scoreSteps.get("Test"+(testNumber + (task.testsFromZero() ? -1 : 0)))); /// backward compatability
				else addTestResult(testNumber, new StepResult(Verdict.WAITING));
			}
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
                testInError = i+1;
            }
            if (groupVerdict == Verdict.OK) {
                groupVerdict = result.getVerdict();
            } else if (groupVerdict == Verdict.PARTIAL && result.getVerdict() != Verdict.OK) {
                groupVerdict = result.getVerdict();
            }
        }
        if (groupVerdict == Verdict.OK) {
		for (int dependencyGroup: task.dependsOn(groupIndex+1)) {
			StepResult dependencyResult = groupResults.get(dependencyGroup-1);
			groupVerdict = dependencyResult.getVerdict();
	    	}
        }


        double groupScore = 0;
        if (task.testsScoring()){
            groupScore = testGroup.getWeight() * checkerSum;
        } else {
            if ((task.minScoring() && !task.sumScoring()) || (task.stopScoringOnFailure() && groupVerdict != Verdict.OK && groupVerdict != Verdict.PARTIAL)) groupScore = testGroup.getWeight() * checkerMin;
			else {
				checkerMin = checkerSum / testGroup.getTestCases().size();
				groupScore = testGroup.getWeight() * checkerMin;
			}
        }

		addGroupResult(groupIndex+1, new StepResult(groupVerdict, ""+testInError, groupTime, groupMemory, groupScore*taskPoints, checkerMin));

		return groupScore;
	}

	public double calculateScore (TaskDetails task) {
		double testsScore = 0.0;
		if (compileResult ==  null && scoreSteps != null) setCompileResult(scoreSteps.get("Compile"));
		boolean finished = true;
		if (compileResult != null && compileResult.getVerdict() != Verdict.CE) {
			groupResults = new ArrayList<>();
			for (int i = 0; i < task.getTestGroups().size(); i++) groupResults.add(new StepResult(Verdict.WAITING));
			for (int i = 0; i < task.getTestGroups().size(); i++) {
				testsScore += calculateGroupScore(i, task.getPoints(), task);
			}
			for (StepResult testResult : getTestResults()) {
				if (testResult.getVerdict() == Verdict.WAITING) {
					finished = false;
					break;
				}
			}
		}
		return calculateFinalScore(testsScore, task.getPoints(), task.getPrecision(), finished);
	}

	public double calculateFinalScore (double testsScore, double taskPoints, int pointsPrecision,  boolean finished) {
		double finalScore = Precision.round(Precision.round(testsScore * taskPoints, 6), pointsPrecision);
		addFinalScore(finalScore, finished);

		return finalScore;
	}
}
