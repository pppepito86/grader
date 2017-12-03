import java.io.File;

import org.pesho.judge.grader.grade.SubmissionGrader;
import org.pesho.judge.task.test.TaskTests;
import org.pesho.judge.task.test.TestCase;
import org.pesho.judge.task.test.TestGroup;

public class Main {
	
	public static void main(String[] args) {
		TaskParser taskParser = new TaskParser(new File("."));
		TestCase[] testCases = new TestCase[taskParser.testsCount()];
		for (int i = 0; i < testCases.length; i++) {
			testCases[i] = new TestCase(taskParser.getInput().get(i).getAbsolutePath(), taskParser.getOutput().get(i).getAbsolutePath());
		}
		TestGroup[] testGroups = new TestGroup[testCases.length];
		for (int i = 0; i < testGroups.length; i++) {
			testGroups[i] = new TestGroup(1.0/testCases.length, testCases[i]);
		}
		TaskTests tests = new TaskTests(100, taskParser.getChecker().getAbsolutePath(), testGroups);
		SubmissionGrader grader = new SubmissionGrader(tests, args[0]);
		double score = grader.grade();
		System.out.println("Score is: " + score);
	}

}