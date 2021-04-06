package org.pesho.grader.test;

import java.io.File;

import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.CommandStatus;

public class ZipTestStep extends TestStep {

	public static final String BINARY_FILE_ENDING = ".zip";

	public static final String EXECUTE_COMMAND_PATTERN = "/usr/bin/unzip -p %s %s";

	public ZipTestStep(File binaryFile, File inputFile, File outputFile, double time, int memory) {
		super(binaryFile, inputFile, outputFile, time, memory);
	}

	@Override
	public String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName(), inputFile.getName().replace(".in", ".out"));
	}
	
	protected StepResult getResult(CommandResult result) {
		if (result.getStatus() != CommandStatus.PROGRAM_ERROR) return super.getResult(result);
		return new StepResult(Verdict.SKIPPED, "File not found");
	}

}
