package org.pesho.grader.test;

import java.io.File;

public class JavaTestStep extends TestStep {

	public static final String EXECUTE_COMMAND_PATTERN = "/usr/bin/java -jar %s";

	public JavaTestStep(File binaryFile, File managerFile, File inputFile, File outputFile, double time, int memory, int processes) {
		super(binaryFile, managerFile, inputFile, outputFile, time, memory, processes);
	}

	@Override
	public String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName());
	}

}
