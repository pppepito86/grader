package org.pesho.grader;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.pesho.grader.step.StepResult;
import org.pesho.grader.task.TaskDetails;
import org.pesho.grader.task.TaskParser;
import org.pesho.grader.task.TestCase;
import org.pesho.grader.task.TestGroup;

public class Main {
	
	public static void main(String[] args) {
		TaskParser taskParser = new TaskParser(new File(args[0]));
		TestCase[] testCases = new TestCase[taskParser.testsCount()];
		for (int i = 0; i < testCases.length; i++) {
			testCases[i] = new TestCase(i+1, taskParser.getInput().get(i).getAbsolutePath(), taskParser.getOutput().get(i).getAbsolutePath());
		}
		TestGroup[] testGroups = new TestGroup[testCases.length];
		for (int i = 0; i < testGroups.length; i++) {
			testGroups[i] = new TestGroup(1.0/testCases.length, testCases[i]);
		}
		TaskDetails tests = new TaskDetails(new Properties(), taskParser.getChecker().getAbsolutePath(), testGroups);
		SubmissionGrader grader = null;
		if (args.length == 2) grader = new SubmissionGrader("1", tests, args[1]);
		else grader = new SubmissionGrader("1", tests, selectSolution(taskParser.getSolutions()));
		grader.grade();
		SubmissionScore score = grader.getScore();
		for (Map.Entry<String, StepResult> entry: score.getScoreSteps().entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().getVerdict());
		}
		System.out.println("Score is: " + new DecimalFormat("#0.00").format(score.getScore()));
	}

	private static String selectSolution(List<File> solutions) {
		System.out.println("Select solution to grade:");
		for (int i = 1; i <= solutions.size(); i++) {
			System.out.println(i + ". " + solutions.get(i-1).getName());
		}
		try (Scanner in = new Scanner(System.in)) {
			return solutions.get(in.nextInt()-1).getAbsolutePath();
		}
	}

}