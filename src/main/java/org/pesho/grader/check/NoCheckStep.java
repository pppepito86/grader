package org.pesho.grader.check;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.pesho.grader.step.Verdict;
import org.zeroturnaround.exec.ProcessExecutor;

public class NoCheckStep extends CheckStep {

	public static final String GRADE_COMMAND_PATTERN = "bash -c 'diff -Z -B -q \"%s\" \"%s\"'";
	
	Verdict verdict;

	public NoCheckStep(File binaryFile, File inputFile, File outputFile, File solutionFile) {
		super(binaryFile, inputFile, outputFile, solutionFile);
	}

	public double execute() {
		try {
			List<String> cmd = Arrays.asList("diff", "-Z", "-B", "-q", outputFile.getAbsolutePath(), solutionFile.getAbsolutePath());
			int exitValue = new ProcessExecutor().readOutput(false).command(cmd).execute().getExitValue();
			if (exitValue == 0) {
				verdict = Verdict.OK;
			} else {
				verdict = Verdict.WA;
			}
		} catch (Exception e) {
			e.printStackTrace();
			verdict = Verdict.SE;
		}
		if (verdict == Verdict.OK) {
			return 1.0;
		} else {
			return 0.0;
		}
	}

	@Override
	public Verdict getVerdict() {
		return verdict;
	}

	protected String getCommand() {
		return String.format(GRADE_COMMAND_PATTERN, outputFile.getAbsolutePath(), solutionFile.getAbsolutePath());
	}

}
