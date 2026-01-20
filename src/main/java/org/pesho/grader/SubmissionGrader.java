package org.pesho.grader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.pesho.grader.compile.ZipLaTexCompileStep;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.grader.task.TaskDetails;
import org.pesho.grader.task.TestCase;
import org.pesho.grader.task.TestGroup;
import org.pesho.grader.test.TestStep;
import org.pesho.grader.test.TestStepFactory;

public class SubmissionGrader {
	
	private String submissionId;
	private boolean isOfficial;
	private TaskDetails taskDetails;
	private List<File> inputFiles;
	private List<File> outputFiles;
	private File originalSourceFile;
	private File binaryFile;
	private SubmissionScore score;
	private GradeListener listener;
	private Optional<Double> compileTime;
	private Optional<Integer> compileMemory;
	private Optional<Double> points;
	private File piperFile;
	
	public SubmissionGrader(String submissionId, Optional<Boolean> isOfficial, TaskDetails taskDetails, String sourceFile, GradeListener listener, String piperFile, Optional<Double> compileTL, Optional<Integer> compileML, Optional<Double> points) {
		this.submissionId = submissionId;
		this.isOfficial = (isOfficial.isPresent() ? isOfficial.get() : true); // backward compatability
		this.taskDetails = taskDetails;
		this.originalSourceFile = new File(sourceFile).getAbsoluteFile();
		this.inputFiles = null;
		this.outputFiles = null;
		this.score = new SubmissionScore("submission");
		this.listener = listener;
		this.compileTime = compileTL;
		this.compileMemory = compileML;
		this.points = points;
		this.piperFile = new File(piperFile);
	}

	public SubmissionGrader(String submissionId, Optional<Boolean> isOfficial, TaskDetails taskDetails, String sourceFile, List<String> inputFiles, List<String> outputFiles, GradeListener listener, String piperFile, Optional<Double> compileTL, Optional<Integer> compileML) {
		this.submissionId = submissionId;
		this.isOfficial = (isOfficial.isPresent() ? isOfficial.get() : true); // backward compatability
		this.taskDetails = taskDetails;
		this.originalSourceFile = new File(sourceFile).getAbsoluteFile();
		this.inputFiles = inputFiles.stream().map(f -> new File(f).getAbsoluteFile()).collect(Collectors.toList());
		this.outputFiles = outputFiles.stream().map(f -> new File(f).getAbsoluteFile()).collect(Collectors.toList());
		this.score = new SubmissionScore("user_tests");
		this.listener = listener;
		this.compileTime = compileTL;
		this.compileMemory = compileML;
		this.points = Optional.empty();
		this.piperFile = new File(piperFile);
	}

	private void failedScore (String message) {
		score.getTestResults().add(0, new StepResult(Verdict.SKIPPED, message));
		score.addFinalScore(0, true);
		if (listener != null) {
			//listener.addFinalScore("Compilation Failed", 0);
			listener.scoreUpdatedWithBlocking(submissionId, score);
		}
	}
	
	public double grade() {
		File sandboxDir = new File(originalSourceFile.getParentFile(), "sandbox_"+originalSourceFile.getName());
		try {
			double score = gradeInternal(sandboxDir);
//			if (score > 0) FileUtils.deleteQuietly(sandboxDir);
			return score;
		} catch (Exception e) {
			e.printStackTrace();
			failedScore(e.getMessage());
			return 0;
		}
		finally {
			FileUtils.deleteQuietly(sandboxDir);
		}
	}

	private void saveUserTestOut (File file) {
		if (file == null) return ;
		File saveFile = new File(originalSourceFile.getParentFile(), "test_user_out");
		if (binaryFile.length() <= 10 * 1024 * 1024L) {
			try {
				FileUtils.copyFile(file, saveFile);
			} catch (IOException e) {
				e.printStackTrace();
				failedScore(e.getMessage());
			}
		}
		else {
			try {
				Files.write(Paths.get(saveFile.getAbsolutePath()), "File larger than 10 MB!\n".getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
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
			failedScore(e.getMessage());
			return 0;
		}
		
		double compileResult = compile(sourceFile, type);
		if (compileResult != 1) {
			score.addFinalScore(compileResult, true);
			if (listener != null) {
				//listener.addFinalScore("Compilation Failed", 0);
				listener.scoreUpdatedWithBlocking(submissionId, score);
			}
			return compileResult;
		}
		
		double finalScore = executeTests(checkerFile, type);
		if (listener != null) {
			//listener.addFinalScore("", finalScore);
			listener.scoreUpdatedWithBlocking(submissionId, score);
		}
		
		return finalScore;
	}

	private double compile(File sourceFile, String type) {
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
		CompileStep compileStep = CompileStepFactory.getInstance(sourceFile, graderDir, compileTL, compileML, type.equals("submission"));
		if (compileStep instanceof ZipLaTexCompileStep) {
			((ZipLaTexCompileStep)compileStep).setTexMode(taskDetails.getTexMode());
		}
		
		sourceStep.execute();
		StepResult result = sourceStep.getResult();
		if (result.getVerdict() == Verdict.OK) {
			compileStep.execute();
			result = compileStep.getResult();
		}
		
		score.setCompileResult(result);
		if (listener != null) {
			boolean blocked = !listener.setCompileResultWithBlocking(submissionId, type, result);
			blocked = !listener.scoreUpdatedWithBlocking(submissionId, score);
			if (blocked == true) return -1;
		}
		if (result.getVerdict() == Verdict.OK) {
			binaryFile = compileStep.getBinaryFile();
			if (type.equals("user_tests") && inputFiles.size() == 0) saveUserTestOut(binaryFile);
			return 1;
		}
		return 0;
	}

	private double getTaskPoints () {
		return points.isPresent() ? points.get() : taskDetails.getPoints();
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
			double testPoints = testWeight * getTaskPoints();
			
			File managerFile = taskDetails.getManager() != null?new File(taskDetails.getManager()) : null;
			
			if (type.equals("submission")) {
				boolean allTestsOk = true;
				for (int dependencyGroup: taskDetails.dependsOn(i+1)) {
					if (dependencyGroup < 1 || dependencyGroup >= i+1) {
						allTestsOk = false;
						break;
					}
					StepResult dependencyResult = this.score.getGroupResults().get(dependencyGroup-1);
					if (dependencyResult.getVerdict() != Verdict.OK && dependencyResult.getVerdict() != Verdict.PARTIAL) {
						allTestsOk = false;
						break;
					}
				}
			
				for (int j = 0; j < testGroup.getTestCases().size(); j++) {
					TestCase testCase = testGroup.getTestCases().get(j);
					StepResult result = executeTest(testCase, managerFile, checkerFile, allTestsOk, testPoints, "submission");
					score.addTestResult(testCase.getNumber() + (taskDetails.testsFromZero() ? 1 : 0), result);
					if (listener != null) {
						boolean blocked = !listener.addTestResultWithBlocking(submissionId, type, testCase.getNumber() + (taskDetails.testsFromZero() ? 1 : 0), result);
						blocked = !listener.scoreUpdatedWithBlocking(submissionId, score);
						if (blocked == true) return -1;
					}
					
					if (result.getVerdict() != Verdict.OK && result.getVerdict() != Verdict.PARTIAL && taskDetails.stopScoringOnFailure()) {
						allTestsOk = false;	
					}
				}
				testsScore += score.calculateGroupScore(i, getTaskPoints(), taskDetails);
			}
			else {
				StepResult result = executeTest(new TestCase(i+1, inputFiles.get(i).getAbsolutePath(), (outputFiles.size() == 0 ? null : outputFiles.get(i).getAbsolutePath())), managerFile, checkerFile, true, testPoints, "user_tests");
				score.addTestResult(i+1, result);
				if (listener != null) {
					boolean blocked = !listener.addTestResultWithBlocking(submissionId, type, i+1, result);
					blocked = !listener.scoreUpdatedWithBlocking(submissionId, score);
					if (blocked == true) return -1;
				}
				score.addGroupResult(i+1, new StepResult(result.getVerdict(), "", result.getTime(), result.getMemory(), testPoints, result.getCheckerOutput()));
				testsScore += testPoints;
			}

			score.calculateFinalScore(testsScore, getTaskPoints(), taskDetails.getPrecision(), false);
			if (listener != null) {
				//listener.addGroupResult(submissionId, type, i+1, score.getGroupResults().get(i));
				boolean blocked = !listener.scoreUpdatedWithBlocking(submissionId, score);
				if (blocked == true) return -1;
			}
		}
		return score.calculateFinalScore(testsScore, getTaskPoints(), taskDetails.getPrecision(), true);
	}
	
	private StepResult executeTest(TestCase testCase, File managerFile, File checkerFile, boolean allTestsOk, double testPoints, String type) {
		if (!allTestsOk) {
			StepResult result = new StepResult(Verdict.SKIPPED);
			return result;
		}
		
		File inputFile = new File(testCase.getInput());
		File outputFile = null;
		if ((type.equals("submission") || (type.equals("user_tests") && testCase.getOutput() != null))) {
			if (testCase.getOutput() == null) {
				try {
					outputFile = File.createTempFile("temp-"+RandomStringUtils.randomAlphabetic(8), ".sol");
					outputFile.deleteOnExit();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else outputFile = new File(testCase.getOutput());
		}
		File solutionFile = new File(binaryFile.getParentFile(), "user_"+ (outputFile != null ? outputFile.getName() : "temp-"+RandomStringUtils.randomAlphabetic(8)+".sol"));
		Double tl = taskDetails.getTime();
		TestStep testStep = TestStepFactory.getInstance(binaryFile, managerFile, piperFile, inputFile, solutionFile, isOfficial, tl, taskDetails.getMemory(), taskDetails.getProcesses(), taskDetails.getOpenFiles(), taskDetails.getIoTime());
		testStep.execute();
		int rejudgeTimes = taskDetails.getRejudgeTimes();
		for (int i = 1; i <= rejudgeTimes; i++) {
			if (testStep.getVerdict() != Verdict.TL || Messages.WALL_CLOCK_TIMEOUT.equals(testStep.getResult().getReason()) || Messages.EXTRA_TIME_LIMIT_EXCEEDED.equals(testStep.getResult().getReason())) break;
			testStep.execute();
		}

		if (testStep.getVerdict() != Verdict.OK) {
			StepResult result = testStep.getResult();
			if (Messages.WALL_CLOCK_TIMEOUT.equals(testStep.getResult().getReason())) result.setTime(null);
			return result;
		}
		
		CheckStep checkerStep = CheckStepFactory.getInstance(checkerFile, inputFile, outputFile, solutionFile);
		checkerStep.execute();
		if (type.equals("submission") && testCase.getOutput() == null) FileUtils.deleteQuietly(outputFile);
		if (type.equals("user_tests") && inputFiles.size() == 1) saveUserTestOut(solutionFile);
		StepResult result = checkerStep.getResult();

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
