package org.pesho.grader;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Precision;
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
	
	private String submissionId;
	private TaskDetails taskDetails;
	private File originalSourceFile;
	private File binaryFile;
	private SubmissionScore score;
	private GradeListener listener;
	private Optional<Double> timeLimit;
	
	public SubmissionGrader(String submissionId, TaskDetails taskTests, String sourceFile) {
		this(submissionId, taskTests, sourceFile, null);
	}
	
	public SubmissionGrader(String submissionId, TaskDetails taskTests, String sourceFile, GradeListener listener) {
		this(submissionId, taskTests, sourceFile, listener, null);
	}
	
	public SubmissionGrader(String submissionId, TaskDetails taskTests, String sourceFile, GradeListener listener, Double tl) {
		this.submissionId = submissionId;
		this.taskDetails = taskTests;
		this.originalSourceFile = new File(sourceFile).getAbsoluteFile();
		this.score = new SubmissionScore();
		this.listener = listener;
		this.timeLimit = Optional.ofNullable(tl);
	}
	
	public double grade() {
		File sandboxDir = new File(originalSourceFile.getParentFile(), "sandbox_"+originalSourceFile.getName());
		try {
			double score = gradeInternal(sandboxDir);
//			if (score > 0) FileUtils.deleteQuietly(sandboxDir);
			return score;
		} finally {
			FileUtils.deleteQuietly(sandboxDir);
		}
	}
	
	public double gradeInternal(File sandboxDir) {
		sandboxDir.mkdirs();
		File sourceFile = new File(sandboxDir, originalSourceFile.getName());
		File originalCheckerFile = new File(taskDetails.getChecker());
		File graderDir = new File(taskDetails.getGraderDir());
		File checkerFile = new File(sandboxDir, originalCheckerFile.getName());
		try {
			FileUtils.copyFile(originalSourceFile, sourceFile);
			if (originalCheckerFile.exists()) {
				FileUtils.copyFile(originalCheckerFile, checkerFile);
				checkerFile.setExecutable(true);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		
		if (compile(sourceFile, graderDir) == 0) {
			score.addFinalScore("Compilation Failed", 0);
			if (listener != null) {
				listener.addFinalScore("Compilation Failed", 0);
				listener.scoreUpdated(submissionId, score);
			}
			return 0;
		}
		
		double testsScore = executeTests(checkerFile);
		double finalScore = 0;
		String verdict = "";
		if (taskDetails.getPoints() == -1) {
			finalScore = Precision.round(testsScore, taskDetails.getPrecision());
		} else {
			finalScore = Precision.round(testsScore * taskDetails.getPoints(), taskDetails.getPrecision());
			
			int percent = (int) (Math.round(100 * finalScore / testsScore) + 0.5);
			verdict = "Accepted";
			if (percent < 100) {
				verdict = percent + "%";
			}
		}
		score.addFinalScore(verdict, finalScore);
		if (listener != null) {
			listener.addFinalScore(verdict, finalScore);
			listener.scoreUpdated(submissionId, score);
		}
		return finalScore;
	}

	private double compile(File sourceFile, File graderDir) {
		CompileStep compileStep = CompileStepFactory.getInstance(sourceFile, graderDir);
		compileStep.execute();
		score.addScoreStep("Compile", compileStep.getResult());
		if (listener != null) {
			listener.addScoreStep("Compile", compileStep.getResult());
			listener.scoreUpdated(submissionId, score);
		}
		if (compileStep.getVerdict() == Verdict.OK) {
			binaryFile = compileStep.getBinaryFile();
			return 1;
		}
		return 0;
	}
	
	private double executeTests(File checkerFile) {
		double score = 0.0;
		double totalWeight = taskDetails.getTestGroups().stream().mapToDouble(g -> g.getWeight()).sum();
		for (TestGroup testGroup: taskDetails.getTestGroups()) {
			double testWeight = testGroup.getWeight()/testGroup.getTestCases().size()/totalWeight;
			double testPoints = testWeight*taskDetails.getPoints();
			Verdict groupVerdict = Verdict.OK;
			double checkerSum = 0.0;

			boolean allTestsOk = true;
			for (TestCase testCase: testGroup.getTestCases()) {
				StepResult result = executeTest(testCase, checkerFile, allTestsOk, testPoints);
				if (result.getVerdict() != Verdict.OK) {
					allTestsOk = false;	
				}
				
				if (result.getVerdict() == Verdict.OK || result.getVerdict() == Verdict.PARTIAL) {
					checkerSum += result.getCheckerOutput();
				} else if (groupVerdict == Verdict.OK) {
					groupVerdict = result.getVerdict();
				}
			}

			if (taskDetails.getPoints() == -1) {
				score += checkerSum;
			} else {
				if (taskDetails.groupsScoring() && groupVerdict == Verdict.OK) {
					score += testGroup.getWeight();
				}
				if (!taskDetails.groupsScoring()) {
//					System.out.println(testGroup.getWeight() + " " + checkerSum + " " + testGroup.getTestCases().size());
					score += testGroup.getWeight() * checkerSum / testGroup.getTestCases().size();
				}
			}
		}
		return score;
	}
	
	private StepResult executeTest(TestCase testCase, File checkerFile, boolean allTestsOk, double testPoints) {
		if (!allTestsOk) {
			StepResult result = new StepResult(Verdict.SKIPPED);
			score.addScoreStep("Test" + testCase.getNumber(), result);
			if (listener != null) {
				listener.addScoreStep("Test" + testCase.getNumber(), result);
				listener.scoreUpdated(submissionId, score);
			}
			return new StepResult(result.getVerdict());
		}
		
		File inputFile = new File(testCase.getInput());
		File outputFile = new File(testCase.getOutput());
		File solutionFile = new File(binaryFile.getParentFile(), "user_"+outputFile.getName());
		Double tl = timeLimit.orElse(taskDetails.getTime());
		TestStep testStep = TestStepFactory.getInstance(binaryFile, inputFile, solutionFile, tl, taskDetails.getMemory());
		testStep.execute();
		if (testStep.getVerdict() == Verdict.TL && tl < 1 && allTestsOk) {
			testStep = TestStepFactory.getInstance(binaryFile, inputFile, solutionFile, tl, taskDetails.getMemory());
			testStep.execute();
		}

		if (testStep.getVerdict() != Verdict.OK) {
			score.addScoreStep("Test" + testCase.getNumber(), testStep.getResult());
			if (listener != null) {
				listener.addScoreStep("Test" + testCase.getNumber(), testStep.getResult());
				listener.scoreUpdated(submissionId, score);
			}
			return new StepResult(testStep.getVerdict());
		}
		CheckStep checkerStep = CheckStepFactory.getInstance(checkerFile, inputFile, outputFile, solutionFile);
		checkerStep.execute();
		StepResult result = checkerStep.getResult();
		result.setTime(testStep.getResult().getTime());
		result.setMemory(testStep.getResult().getMemory());
		result.setExitCode(testStep.getResult().getExitCode());

		if (taskDetails.getPoints() == -1) {
			if (Double.compare(result.getCheckerOutput(), -1.0) == 0) result.setVerdict(Verdict.WA);
		}
		
		if (result.getVerdict() == Verdict.OK) result.setPoints(testPoints);
		if (result.getVerdict() == Verdict.PARTIAL) result.setPoints(result.getCheckerOutput() * testPoints);

		score.addScoreStep("Test" + testCase.getNumber(), result);
		if (listener != null) {
			listener.addScoreStep("Test" + testCase.getNumber(), result);
			listener.scoreUpdated(submissionId, score);
		}
		return result;
	}
	
	public SubmissionScore getScore() {
		return score;
	}

}
