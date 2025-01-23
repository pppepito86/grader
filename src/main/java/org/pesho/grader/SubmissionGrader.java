package org.pesho.grader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
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
	private List<File> inputFiles;
	private List<File> outputFiles;
	private File originalSourceFile;
	private File binaryFile;
	private SubmissionScore score;
	private GradeListener listener;
	private Optional<Double> compileTime;
	private Optional<Integer> compileMemory;
	private File piperFile;
	
	public SubmissionGrader(String submissionId, TaskDetails taskDetails, String sourceFile, GradeListener listener, String piperFile, Optional<Double> compileTL, Optional<Integer> compileML) {
		this.submissionId = submissionId;
		this.taskDetails = taskDetails;
		this.originalSourceFile = new File(sourceFile).getAbsoluteFile();
		this.inputFiles = null;
		this.outputFiles = null;
		this.score = new SubmissionScore("submission");
		this.listener = listener;
		this.compileTime = compileTL;
		this.compileMemory = compileML;
		this.piperFile = new File(piperFile);
	}

	public SubmissionGrader(String submissionId, TaskDetails taskDetails, String sourceFile, List<String> inputFiles, List<String> outputFiles, GradeListener listener, String piperFile, Optional<Double> compileTL, Optional<Integer> compileML) {
		this.submissionId = submissionId;
		this.taskDetails = taskDetails;
		this.originalSourceFile = new File(sourceFile).getAbsoluteFile();
		this.inputFiles = inputFiles.stream().map(f -> new File(f).getAbsoluteFile()).collect(Collectors.toList());
		this.outputFiles = outputFiles.stream().map(f -> new File(f).getAbsoluteFile()).collect(Collectors.toList());
		this.score = new SubmissionScore("user_tests");
		this.listener = listener;
		this.compileTime = compileTL;
		this.compileMemory = compileML;
		this.piperFile = new File(piperFile);
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
		String type = (inputFiles == null ? "submission" : "user_tests");
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
			score.addFinalScore(0, true);
			if (listener != null) {
				//listener.addFinalScore("Compilation Failed", 0);
				listener.scoreUpdated(submissionId, score);
			}
			return 0;
		}
		
		double finalScore = executeTests(checkerFile, type);
		if (listener != null) {
			//listener.addFinalScore("", finalScore);
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
			//listener.setCompileResult(result);
			listener.scoreUpdated(submissionId, score);
		}
		if (result.getVerdict() == Verdict.OK) {
			binaryFile = compileStep.getBinaryFile();
			return 1;
		}
		return 0;
	}
	
	private double executeTests(File checkerFile, String type) {
		int groupsCount = (type.equals("submission") ? taskDetails.getTestGroups().size() : inputFiles.size());
		int testsCount = (type.equals("submission") ? taskDetails.getTestGroups().stream().mapToInt(g -> g.getTestCases().size()).sum() : inputFiles.size());
		score.startingTests(groupsCount, testsCount);
		
		double testsScore = 0.0;
		double totalWeight = (type.equals("submission") ? taskDetails.getTestGroups().stream().mapToDouble(g -> g.getWeight()).sum() : inputFiles.size());
		for (int i = 0; i < groupsCount; i++) {
			TestGroup testGroup = (type.equals("submission") ? taskDetails.getTestGroups().get(i) : null);
			double testWeight = (type.equals("submission") ? testGroup.getWeight()/testGroup.getTestCases().size()/totalWeight : 1/totalWeight);
			double testPoints = testWeight*taskDetails.getPoints();
			
			File managerFile = taskDetails.getManager() != null?new File(taskDetails.getManager()) : null;
			
			if (type.equals("submission")) {
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
					StepResult result = executeTest(testCase, managerFile, checkerFile, allTestsOk, testPoints, "submission");
					score.addTestResult(testCase.getNumber(), result);
					if (listener != null) {
						//listener.addTestResult(testCase.getNumber(), result);
						listener.scoreUpdated(submissionId, score);
					}
					
					if (result.getVerdict() != Verdict.OK && result.getVerdict() != Verdict.PARTIAL && taskDetails.stopScoringOnFailure()) {
						allTestsOk = false;	
					}
				}
				testsScore += score.calculateGroupScore(i, taskDetails);
			}
			else {
				StepResult result = executeTest(new TestCase(i+1, inputFiles.get(i).getAbsolutePath(), (outputFiles.size() == 0 ? null : outputFiles.get(i).getAbsolutePath())), managerFile, checkerFile, true, testPoints, "user_tests");
				score.addTestResult(i+1, result);
				score.addGroupResult(i+1, new StepResult(result.getVerdict(), "", result.getTime(), result.getMemory(), testPoints, result.getCheckerOutput()));
				testsScore += testPoints;
			}

			score.calculateFinalScore(taskDetails, testsScore, false);
			if (listener != null) {
				//listener.addGroupResult(i+1, score.getGroupResults().get(i));
				listener.scoreUpdated(submissionId, score);
			}
		}
		return score.calculateFinalScore(taskDetails, testsScore, true);
	}
	
	private StepResult executeTest(TestCase testCase, File managerFile, File checkerFile, boolean allTestsOk, double testPoints, String type) {
		if (!allTestsOk) {
			StepResult result = new StepResult(Verdict.SKIPPED);
			return result;
		}
		
		File inputFile = new File(testCase.getInput());
		File outputFile = null;
		if (testCase.getOutput() == null) {
			try {
				outputFile = File.createTempFile("temp-"+RandomStringUtils.randomAlphabetic(8), ".sol");
				outputFile.deleteOnExit();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else outputFile = new File(testCase.getOutput());
		File solutionFile = new File(binaryFile.getParentFile(), "user_"+outputFile.getName());
		Double tl = taskDetails.getTime();
		TestStep testStep = TestStepFactory.getInstance(binaryFile, managerFile, piperFile, inputFile, solutionFile, tl, taskDetails.getMemory(), taskDetails.getProcesses(), taskDetails.getOpenFiles(), taskDetails.getIoTime());
		testStep.execute();
		if (testStep.getVerdict() == Verdict.TL && !Messages.WALL_CLOCK_TIMEOUT.equals(testStep.getResult().getReason()) && tl < 1) {
			testStep = TestStepFactory.getInstance(binaryFile, managerFile, piperFile, inputFile, solutionFile, tl, taskDetails.getMemory(), taskDetails.getProcesses(), taskDetails.getOpenFiles(), taskDetails.getIoTime());
			testStep.execute();
		}
		if (testStep.getVerdict() == Verdict.TL) {
			int rejudgeTimes = taskDetails.getRejudgeTimes();
			for (int i = 2; i <= rejudgeTimes; i++) {
				if (testStep.getVerdict() != Verdict.TL || Messages.WALL_CLOCK_TIMEOUT.equals(testStep.getResult().getReason()) || Messages.EXTRA_TIME_LIMIT_EXCEEDED.equals(testStep.getResult().getReason())) break;
				testStep.execute();
			}
		}
		
		if (testStep.getVerdict() != Verdict.OK) {
			StepResult result = testStep.getResult();
			if (Messages.WALL_CLOCK_TIMEOUT.equals(testStep.getResult().getReason())) result.setTime(null);
			return result;
		}
		
		StepResult result = new StepResult();
		if (type.equals("submission") || (type.equals("user_tests") && testCase.getOutput() != null)) {
			CheckStep checkerStep = CheckStepFactory.getInstance(checkerFile, inputFile, outputFile, solutionFile);
			checkerStep.execute();
			if (testCase.getOutput() == null) FileUtils.deleteQuietly(outputFile);
			result = checkerStep.getResult();
		}
		else {
			try {
				result = new StepResult(Verdict.OK, FileUtils.readFileToString(solutionFile, Charset.forName("UTF-8")), "");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		result.setTime(testStep.getResult().getTime());
		result.setMemory(testStep.getResult().getMemory());
		result.setExitCode(testStep.getResult().getExitCode());
				
/*		if (taskDetails.isPartial() && result.getVerdict() == Verdict.WA) result.setVerdict(Verdict.PARTIAL); 

		if (taskDetails.getPoints() == -1) {
			if (Double.compare(result.getCheckerOutput(), -1.0) == 0) result.setVerdict(Verdict.WA);
		}
*/
		
		if (result.getVerdict() == Verdict.OK) result.setPoints(testPoints);
		if (result.getVerdict() == Verdict.PARTIAL) result.setPoints(result.getCheckerOutput() * testPoints);

		return result;
	}
	
	public SubmissionScore getScore() {
		return score;
	}

}
