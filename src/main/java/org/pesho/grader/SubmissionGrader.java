package org.pesho.grader;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Precision;
import org.pesho.sandbox.Messages;
import org.pesho.grader.check.CheckStep;
import org.pesho.grader.check.CheckStepFactory;
import org.pesho.grader.compile.CompileStep;
import org.pesho.grader.compile.CompileStepFactory;
import org.pesho.grader.compile.SourceStep;
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
	private Optional<Double> compileTime;
	private Optional<Integer> compileMemory;
	
	public SubmissionGrader(String submissionId, TaskDetails taskTests, String sourceFile) {
		this(submissionId, taskTests, sourceFile, null);
	}
	
	public SubmissionGrader(String submissionId, TaskDetails taskTests, String sourceFile, GradeListener listener) {
		this(submissionId, taskTests, sourceFile, listener, Optional.ofNullable(null), Optional.ofNullable(null));
	}
	
	public SubmissionGrader(String submissionId, TaskDetails taskDetails, String sourceFile, GradeListener listener, Optional<Double> compileTL, Optional<Integer> compileML) {
		this.submissionId = submissionId;
		this.taskDetails = taskDetails;
		this.originalSourceFile = new File(sourceFile).getAbsoluteFile();
		this.score = new SubmissionScore();
		this.listener = listener;
		this.compileTime = compileTL;
		this.compileMemory = compileML;
	}
	
	public double grade(String piperDir) {
		File sandboxDir = new File(originalSourceFile.getParentFile(), "sandbox_"+originalSourceFile.getName());
		try {
			double score = gradeInternal(sandboxDir, piperDir);
//			if (score > 0) FileUtils.deleteQuietly(sandboxDir);
			return score;
		} finally {
			FileUtils.deleteQuietly(sandboxDir);
		}
	}
	
	public File getGrader(boolean compile) {
		if (taskDetails.getGraderDir() == null) return null;

		File graderDir = new File(taskDetails.getGraderDir());
		if (taskDetails.getAllowedExtensions().size() > 1) {
			if (originalSourceFile.getName().toLowerCase().endsWith(".java")) graderDir = new File(graderDir, "java");
			else graderDir = new File(graderDir, "cpp");
		}
		
		boolean isCompile = !new File(graderDir, "grader").exists();
		if (compile == isCompile) return graderDir;
		
		return null;
	}
	
	public double gradeInternal(File sandboxDir, String piperDir) {
		sandboxDir.mkdirs();
		File sourceFile = new File(sandboxDir, originalSourceFile.getName());
		File checkerFile = null;
		try {
			FileUtils.copyFile(originalSourceFile, sourceFile);
			
			if (taskDetails.getChecker() != null) {
				File originalCheckerFile = new File(taskDetails.getChecker());
				if (originalCheckerFile.exists() && originalCheckerFile.isFile()) {
					checkerFile = new File(sandboxDir, originalCheckerFile.getName());
					FileUtils.copyFile(originalCheckerFile, checkerFile);
					checkerFile.setExecutable(true);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		
		if (compile(sourceFile) == 0) {
			score.addFinalScore("Compilation Failed", 0);
			if (listener != null) {
				listener.addFinalScore("Compilation Failed", 0);
				listener.scoreUpdated(submissionId, score);
			}
			return 0;
		}
		
		double finalScore = executeTests(checkerFile, piperDir);
		if (listener != null) {
			listener.addFinalScore("", finalScore);
			listener.scoreUpdated(submissionId, score);
		}
		return finalScore;
	}

	private double compile(File sourceFile) {
		SourceStep sourceStep = new SourceStep(sourceFile, taskDetails.getBlacklistedWords());
		File graderDir = taskDetails.getGraderDir() != null ? new File(taskDetails.getGraderDir()):null;
		Map<String, Double> compileTL = taskDetails.getCompileTime();
		if (compileTime.isPresent()) {
			for (String lang : compileTL.keySet()) {
				compileTL.put(lang, compileTime.get());
			}
		}
		Map<String, Integer> compileML = taskDetails.getCompileMemory();
		if (compileMemory.isPresent()) {
			for (String lang : compileML.keySet()) {
				compileML.put(lang, compileMemory.get());
			}
		}
		CompileStep compileStep = CompileStepFactory.getInstance(sourceFile, graderDir, compileTL, compileML);
		
		sourceStep.execute();
		StepResult result = sourceStep.getResult();
		if (result.getVerdict() == Verdict.OK) {
			compileStep.execute();
			result = compileStep.getResult();
		}
		
		score.setCompileResult(result);
		if (listener != null) {
			listener.setCompileResult(result);
			listener.scoreUpdated(submissionId, score);
		}
		if (result.getVerdict() == Verdict.OK) {
			binaryFile = compileStep.getBinaryFile();
			return 1;
		}
		return 0;
	}
	
	private double executeTests(File checkerFile, String piperDir) {
		int groupsCount = taskDetails.getTestGroups().size();
		int testsCount = taskDetails.getTestGroups().stream().mapToInt(g -> g.getTestCases().size()).sum();
		score.startingTests(groupsCount, testsCount);
		
		double totalWeight = taskDetails.getTestGroups().stream().mapToDouble(g -> g.getWeight()).sum();
		for (int i = 0; i < taskDetails.getTestGroups().size(); i++) {
			TestGroup testGroup = taskDetails.getTestGroups().get(i);
			double testWeight = testGroup.getWeight()/testGroup.getTestCases().size()/totalWeight;
			double testPoints = testWeight*taskDetails.getPoints();
			
			File managerFile = taskDetails.getManager() != null?new File(taskDetails.getManager()) : null;
			File piperFile = new File(piperDir+"/piper");
			
			boolean allTestsOk = true;
			for (int dependencyGroup: taskDetails.dependsOn(i+1)) {
				StepResult dependencyResult = this.score.getGroupResults().get(dependencyGroup-1);
				if (dependencyResult.getVerdict() != Verdict.OK && dependencyResult.getVerdict() != Verdict.PARTIAL) {
					allTestsOk = false;
					break;
				}
			}
			
			for (int j = 0; j < testGroup.getTestCases().size(); j++) {
				TestCase testCase = testGroup.getTestCases().get(j);
				StepResult result = executeTest(testCase, managerFile, piperFile, checkerFile, allTestsOk, testPoints);
				
				if (result.getVerdict() != Verdict.OK && result.getVerdict() != Verdict.PARTIAL && taskDetails.stopScoringOnFailure()) {
					allTestsOk = false;	
				}
			}
		}
		return score.calculateScore(taskDetails);
	}
	
	private StepResult executeTest(TestCase testCase, File managerFile, File piperFile, File checkerFile, boolean allTestsOk, double testPoints) {
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
		Double tl = taskDetails.getTime();
		TestStep testStep = TestStepFactory.getInstance(binaryFile, managerFile, piperFile, inputFile, solutionFile, tl, taskDetails.getMemory(), taskDetails.getProcesses(), taskDetails.getIoTime());
		testStep.execute();
		if (testStep.getVerdict() == Verdict.TL && tl < 1 && allTestsOk) {
			testStep = TestStepFactory.getInstance(binaryFile, managerFile, piperFile, inputFile, solutionFile, tl, taskDetails.getMemory(), taskDetails.getProcesses(), taskDetails.getIoTime());
			testStep.execute();
		}
		
		if (testStep.getVerdict() != Verdict.OK) {
			score.addTestResult(testCase.getNumber(), testStep.getResult());
			if (listener != null) {
				listener.addTestResult(testCase.getNumber(), testStep.getResult());
				listener.scoreUpdated(submissionId, score);
			}
			StepResult result = new StepResult(testStep.getVerdict());
			if (!Messages.WALL_CLOCK_TIMEOUT.equals(testStep.getResult().getReason())) result.setTime(testStep.getResult().getTime());
			result.setMemory(testStep.getResult().getMemory());
			result.setExitCode(testStep.getResult().getExitCode());
			return result;
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
