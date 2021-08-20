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
	
	public SubmissionGrader(String submissionId, TaskDetails taskDetails, String sourceFile, GradeListener listener, Double tl) {
		this.submissionId = submissionId;
		this.taskDetails = taskDetails;
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
		File graderDir = taskDetails.getGraderDir() != null && !new File(taskDetails.getGraderDir(), "grader").exists() ?new File(taskDetails.getGraderDir()) : null;
		File checkerFile = null;
		try {
			FileUtils.copyFile(originalSourceFile, sourceFile);
			
			if (taskDetails.getChecker() != null) {
				File originalCheckerFile = new File(taskDetails.getChecker());
				if (originalCheckerFile.exists()) {
					checkerFile = new File(sandboxDir, originalCheckerFile.getName());
					FileUtils.copyFile(originalCheckerFile, checkerFile);
					checkerFile.setExecutable(true);
				}
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
		score.setCompileResult(compileStep.getResult());
		if (listener != null) {
			listener.setCompileResult(compileStep.getResult());
			listener.scoreUpdated(submissionId, score);
		}
		if (compileStep.getVerdict() == Verdict.OK) {
			binaryFile = compileStep.getBinaryFile();
			return 1;
		}
		return 0;
	}
	
	private double executeTests(File checkerFile) {
		int groupsCount = taskDetails.getTestGroups().size();
		int testsCount = taskDetails.getTestGroups().stream().mapToInt(g -> g.getTestCases().size()).sum();
		score.startingTests(groupsCount, testsCount);
		
		double score = 0.0;
		boolean accepted = true;
		double totalWeight = taskDetails.getTestGroups().stream().mapToDouble(g -> g.getWeight()).sum();
		for (int i = 0; i < taskDetails.getTestGroups().size(); i++) {
			TestGroup testGroup = taskDetails.getTestGroups().get(i);
			double testWeight = testGroup.getWeight()/testGroup.getTestCases().size()/totalWeight;
			double testPoints = testWeight*taskDetails.getPoints();
			double checkerSum = 0.0;
			double checkerMin = testGroup.getTestCases().size() != 0 ? 1.0 : 0.0;
			
			Verdict groupVerdict = Verdict.OK;
			Double groupTime = null;
			Long groupMemory = null;
			Integer testInError = null;

			File graderFile = taskDetails.getGraderDir() != null && new File(taskDetails.getGraderDir(), "grader").exists() ? new File(taskDetails.getGraderDir(), "grader") : null;			
			
			boolean allTestsOk = true;
			for (int j = 0; j < testGroup.getTestCases().size(); j++) {
				TestCase testCase = testGroup.getTestCases().get(j);
				StepResult result = executeTest(testCase, graderFile, checkerFile, allTestsOk, testPoints);
				
				if (result.getVerdict() != Verdict.OK && result.getVerdict() != Verdict.PARTIAL && taskDetails.stopScoringOnFailure()) {
					allTestsOk = false;	
				}
				
				checkerMin = Math.min(checkerMin, result.getCheckerOutput());
				checkerSum += result.getCheckerOutput();
				
				if (groupVerdict == Verdict.OK || groupVerdict == Verdict.PARTIAL) {
					if (result.getVerdict() != Verdict.TL && result.getTime() != null) {
						if (groupTime == null) groupTime = result.getTime();
						else groupTime = Math.max(groupTime, result.getTime());
					}
					if (result.getMemory() != null) {
						if (groupMemory == null) groupMemory = result.getMemory();
						else groupMemory = Math.max(groupMemory, result.getMemory());
					}
				} else if (testInError == null) {
					testInError = j+1;
				}
				if (groupVerdict == Verdict.OK) {
					groupVerdict = result.getVerdict();
				} else if (groupVerdict == Verdict.PARTIAL && result.getVerdict() != Verdict.OK) {
					groupVerdict = result.getVerdict();
				}
			}

			double groupScore = 0;
			if (groupVerdict != Verdict.OK) accepted = false;
			if (taskDetails.getPoints() == -1) {
				groupScore = checkerSum;
				score += checkerSum;
			} else if (taskDetails.testsScoring() || taskDetails.sumScoring()){
				groupScore = testGroup.getWeight() * checkerSum / testGroup.getTestCases().size();
				if (taskDetails.sumScoring() && groupVerdict != Verdict.OK && Double.compare(groupScore, 0.0) != 0) groupVerdict = Verdict.PARTIAL;
			} else {
				groupScore = testGroup.getWeight() * checkerMin;
			}
			score += groupScore;
			this.score.addGroupResult(i+1, new StepResult(groupVerdict, ""+testInError, groupTime, groupMemory, groupScore*taskDetails.getPoints()));
		}
		if (taskDetails.icpcScoring() && !accepted) return 0;
		return score;
	}
	
	private StepResult executeTest(TestCase testCase, File graderFile, File checkerFile, boolean allTestsOk, double testPoints) {
		if (!allTestsOk) {
			StepResult result = new StepResult(Verdict.SKIPPED);
			score.addTestResult(testCase.getNumber(), result);
			if (listener != null) {
				listener.addTestResult(testCase.getNumber(), result);
				listener.scoreUpdated(submissionId, score);
			}
			return new StepResult(result.getVerdict());
		}
		
		File inputFile = new File(testCase.getInput());
		File outputFile = new File(testCase.getOutput());
		File solutionFile = new File(binaryFile.getParentFile(), "user_"+outputFile.getName());
		Double tl = timeLimit.orElse(taskDetails.getTime());
		TestStep testStep = TestStepFactory.getInstance(binaryFile, graderFile, inputFile, solutionFile, tl, taskDetails.getMemory(), taskDetails.getIoTime());
		testStep.execute();
		if (testStep.getVerdict() == Verdict.TL && tl < 1 && allTestsOk) {
			testStep = TestStepFactory.getInstance(binaryFile, graderFile, inputFile, solutionFile, tl, taskDetails.getMemory(), taskDetails.getIoTime());
			testStep.execute();
		}
		
		if (testStep.getVerdict() != Verdict.OK) {
			score.addTestResult(testCase.getNumber(), testStep.getResult());
			if (listener != null) {
				listener.addTestResult(testCase.getNumber(), testStep.getResult());
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
				
//		if (taskDetails.isPartial() && result.getVerdict() == Verdict.WA) result.setVerdict(Verdict.PARTIAL); 

		if (taskDetails.getPoints() == -1) {
			if (Double.compare(result.getCheckerOutput(), -1.0) == 0) result.setVerdict(Verdict.WA);
		}
		
		if (result.getVerdict() == Verdict.OK) result.setPoints(testPoints);
		if (result.getVerdict() == Verdict.PARTIAL) result.setPoints(result.getCheckerOutput() * testPoints);

		score.addTestResult(testCase.getNumber(), result);
		if (listener != null) {
			listener.addTestResult(testCase.getNumber(), result);
			listener.scoreUpdated(submissionId, score);
		}
		return result;
	}
	
	public SubmissionScore getScore() {
		return score;
	}

}
