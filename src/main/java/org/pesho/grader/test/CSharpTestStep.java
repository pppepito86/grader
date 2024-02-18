package org.pesho.grader.test;

import java.io.File;

public class CSharpTestStep extends TestStep {

	public static final String EXECUTE_COMMAND_PATTERN = "mono %s";

	public CSharpTestStep(File binaryFile, File managerFile, File piperFile, File inputFile, File outputFile, double time, int memory, int processes) {
		super(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, processes);
	}

	@Override
	public String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName());
	}

}
