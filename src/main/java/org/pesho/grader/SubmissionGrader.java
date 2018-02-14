package org.pesho.grader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.check.CheckStep;
import org.pesho.grader.check.CheckStepFactory;
import org.pesho.grader.compile.CompileStep;
import org.pesho.grader.compile.CompileStepFactory;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.grader.task.TaskDetails;
import org.pesho.grader.task.TestCase;
import org.pesho.grader.task.TestGroup;
import org.pesho.grader.test.TestStep;
import org.pesho.grader.test.TestStepFactory;

public class SubmissionGrader {
	
	private TaskDetails taskDetails;
	private File originalSourceFile;
	private File binaryFile;
	private SubmissionScore score;
	private GradeListener listener;
	
	public SubmissionGrader(TaskDetails taskTests, String sourceFile) {
		this(taskTests, sourceFile, null);
	}

	public SubmissionGrader(TaskDetails taskTests, String sourceFile, GradeListener listener) {
		this.taskDetails = taskTests;
		this.originalSourceFile = new File(sourceFile).getAbsoluteFile();
		this.score = new SubmissionScore();
		this.listener = listener;
	}
	
	public double grade() {
		File sandboxDir = new File(originalSourceFile.getParentFile(), "sandbox_"+originalSourceFile.getName());
		try {
			double score = gradeInternal(sandboxDir);
			if (score > 0) FileUtils.deleteQuietly(sandboxDir);
			return score;
		} finally {
			FileUtils.deleteQuietly(sandboxDir);
		}
	}
	
	public double gradeInternal(File sandboxDir) {
		sandboxDir.mkdirs();
		File sourceFile = new File(sandboxDir, originalSourceFile.getName());
		File originalCheckerFile = new File(taskDetails.getChecker());
		File checkerFile = new File(sandboxDir, originalCheckerFile.getName());
		try {
			FileUtils.copyFile(originalSourceFile, sourceFile);
			if (originalCheckerFile.exists()) {
				FileUtils.copyFile(originalCheckerFile, checkerFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		
		if (compile(sourceFile) == 0) {
			score.addFinalScore("Compilation Failed", 0);
			if (listener != null) {
				listener.addFinalScore("Compilation Failed", 0);
			}
			return 0;
		}
		
		double testsScore = executeTests(checkerFile);
		double finalScore = testsScore * taskDetails.getPoints();
		int percent = (int) Math.round(100 * finalScore / testsScore);
		String verdict = "Accepted";
		if (percent < 100) {
			verdict = percent + "%";
		}
		score.addFinalScore(verdict, finalScore);
		if (listener != null) {
			listener.addFinalScore(verdict, finalScore);
		}
		return finalScore;
	}

	private double compile(File sourceFile) {
		CompileStep compileStep = CompileStepFactory.getInstance(sourceFile);
		compileStep.execute();
		score.addScoreStep("Compile", compileStep.getResult());
		if (listener != null) {
			listener.addScoreStep("Compile", compileStep.getResult());
		}
		if (compileStep.getVerdict() == Verdict.OK) {
			binaryFile = compileStep.getBinaryFile();
			return 1;
		}
		return 0;
	}
	
	private double executeTests(File checkerFile) {
		double score = 0.0;
		for (TestGroup testGroup: taskDetails.getTestGroups()) {
			Verdict groupVerdict = Verdict.OK;
			
			for (TestCase testCase: testGroup.getTestCases()) {
				Verdict verdict = executeTest(testCase, checkerFile);
				if (verdict != Verdict.OK) {
					groupVerdict = verdict;
					break;
				}
			}
			if (groupVerdict == Verdict.OK) score += testGroup.getWeight();
		}
		return score;
	}
	
	private Verdict executeTest(TestCase testCase, File checkerFile) {
		File inputFile = new File(testCase.getInput());
		File outputFile = new File(testCase.getOutput());
		File solutionFile = new File(binaryFile.getParentFile(), "user_"+outputFile.getName());
		TestStep testStep = TestStepFactory.getInstance(binaryFile, inputFile, solutionFile, taskDetails.getTime(), taskDetails.getMemory());
		testStep.execute();
		if (testStep.getVerdict() != Verdict.OK) {
			score.addScoreStep("Test" + testCase.getNumber(), testStep.getResult());
			if (listener != null) {
				listener.addScoreStep("Test" + testCase.getNumber(), testStep.getResult());
			}
			return testStep.getVerdict();
		}
		CheckStep checkerStep = CheckStepFactory.getInstance(checkerFile, inputFile, outputFile, solutionFile);
		checkerStep.execute();
		StepResult result = checkerStep.getResult();
		result.setTime(testStep.getResult().getTime());
		score.addScoreStep("Test" + testCase.getNumber(), result);
		if (listener != null) {
			listener.addScoreStep("Test" + testCase.getNumber(), result);
		}
		return checkerStep.getVerdict();
	}
	
	public SubmissionScore getScore() {
		return score;
	}

}
