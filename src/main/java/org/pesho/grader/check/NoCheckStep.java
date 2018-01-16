package org.pesho.grader.check;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.zeroturnaround.exec.ProcessExecutor;

public class NoCheckStep extends CheckStep {

	public static final String GRADE_COMMAND_PATTERN = "bash -c 'diff -Z -B -q \"%s\" \"%s\"'";
	
	protected StepResult result;
	
	public NoCheckStep(File binaryFile, File inputFile, File outputFile, File solutionFile) {
		super(binaryFile, inputFile, outputFile, solutionFile);
	}

	public void execute() {
		try {
			List<String> cmd = Arrays.asList("diff", "-Z", "-B", "-q", outputFile.getAbsolutePath(), solutionFile.getAbsolutePath());
			int exitValue = new ProcessExecutor().readOutput(false).command(cmd).execute().getExitValue();
			if (exitValue == 0) {
				result = new StepResult(Verdict.OK);
			} else {
				result = new StepResult(Verdict.WA, readOutput());
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = new StepResult(Verdict.SE, e.getMessage());
		}
	}

	@Override
	public StepResult getResult() {
		return result;
	}
	
	@Override
	public Verdict getVerdict() {
		return result.getVerdict();
	}

	protected String getCommand() {
		return String.format(GRADE_COMMAND_PATTERN, outputFile.getAbsolutePath(), solutionFile.getAbsolutePath());
	}

}
