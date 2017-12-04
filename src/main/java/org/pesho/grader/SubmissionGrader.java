package org.pesho.grader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.check.CheckStep;
import org.pesho.grader.check.CheckStepFactory;
import org.pesho.grader.compile.CompileStep;
import org.pesho.grader.compile.CompileStepFactory;
import org.pesho.grader.task.TaskTests;
import org.pesho.grader.task.TestCase;
import org.pesho.grader.task.TestGroup;
import org.pesho.grader.test.TestStep;
import org.pesho.grader.test.TestStepFactory;

public class SubmissionGrader {
	
	private TaskTests taskTests;
	private File originalSourceFile;
	
	public SubmissionGrader(TaskTests taskTests, String sourceFile) {
		this.taskTests = taskTests;
		this.originalSourceFile = new File(sourceFile).getAbsoluteFile();
	}

	public double grade() {
		File sandboxDir = new File(originalSourceFile.getParentFile(), "sandbox_"+originalSourceFile.getName());
		sandboxDir.mkdirs();
		File sourceFile = new File(sandboxDir, originalSourceFile.getName());
		File originalCheckerFile = new File(taskTests.getChecker());
		File checkerFile = new File(sandboxDir, originalCheckerFile.getName());
		try {
			FileUtils.copyFile(originalSourceFile, sourceFile);
			FileUtils.copyFile(originalCheckerFile, checkerFile);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		
		System.out.print("Compilation ");
		CompileStep compileStep = CompileStepFactory.getInstance(sourceFile);
		double compileScore = compileStep.execute();
		System.out.println(" - " + compileStep.getVerdict());
		if (compileScore != 1) return 0;
		
		File binaryFile = compileStep.getBinaryFile();
		double score = executeTests(binaryFile, checkerFile);
		return score * taskTests.getPoints();
	}

	private double executeTests(File binaryFile, File checkerFile) {
		double score = 0.0;
		int count = 1;
		for (TestGroup testGroup: taskTests.getTestGroups()) {
			boolean hasFailed = false;
			
			for (TestCase testCase: testGroup.getTestCases()) {
				System.out.print("Test #"+ count++);
				File inputFile = new File(testCase.getInput());
				File outputFile = new File(testCase.getOutput());
				File solutionFile = new File(binaryFile.getParentFile(), "user_"+outputFile.getName());
				TestStep testStep = TestStepFactory.getInstance(binaryFile, inputFile, solutionFile);
				double testResult = testStep.execute();
				if (testResult != 1) {
					System.out.println(" - " + testStep.getVerdict());
					hasFailed = true;
					break;
				}
				
				CheckStep checkerStep = CheckStepFactory.getInstance(checkerFile, inputFile, outputFile, solutionFile);
				double checkerResult = checkerStep.execute();
				if (checkerResult != 1) {
					System.out.println(" - " + checkerStep.getVerdict());
					hasFailed = true;
					break;
				}
				System.out.println(" - " + checkerStep.getVerdict());
			}
			if (!hasFailed) score += testGroup.getWeight();
		}
		return score;
	}

}
