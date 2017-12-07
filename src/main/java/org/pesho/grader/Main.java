package org.pesho.grader;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

import org.pesho.grader.task.TaskParser;
import org.pesho.grader.task.TaskTests;
import org.pesho.grader.task.TestCase;
import org.pesho.grader.task.TestGroup;

public class Main {
	
	public static void main(String[] args) {
		TaskParser taskParser = new TaskParser(new File(args[0]));
//		for (int i = 0; i < taskParser.testsCount(); i++) {
//			System.out.println(taskParser.getInput().get(i) + " " + taskParser.getOutput().get(i));
//		}
		TestCase[] testCases = new TestCase[taskParser.testsCount()];
		for (int i = 0; i < testCases.length; i++) {
			testCases[i] = new TestCase(taskParser.getInput().get(i).getAbsolutePath(), taskParser.getOutput().get(i).getAbsolutePath());
		}
		TestGroup[] testGroups = new TestGroup[testCases.length];
		for (int i = 0; i < testGroups.length; i++) {
			testGroups[i] = new TestGroup(1.0/testCases.length, testCases[i]);
		}
		TaskTests tests = new TaskTests(100, taskParser.getChecker().getAbsolutePath(), testGroups);
		SubmissionGrader grader = null;
		if (args.length == 2) grader = new SubmissionGrader(tests, args[1]);
		else grader = new SubmissionGrader(tests, selectSolution(taskParser.getSolutions()));
		double score = grader.grade();
		System.out.println("Score is: " + new DecimalFormat("#.00").format(score));
	}

	private static String selectSolution(List<File> solutions) {
		System.out.println("Select solution to grade:");
		for (int i = 1; i <= solutions.size(); i++) {
			System.out.println(i + ". " + solutions.get(i-1).getName());
		}
		Scanner in = new Scanner(System.in);
		return solutions.get(in.nextInt()-1).getAbsolutePath();
	}

}