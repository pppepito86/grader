package org.pesho.grader.test;

import java.io.File;

public class CppTestStep extends TestStep {

	public static final String EXECUTE_COMMAND_PATTERN = "./%s";

	public CppTestStep(File binaryFile, File inputFile, File outputFile) {
		super(binaryFile, inputFile, outputFile);
	}

	@Override
	public String[] getCommands() {
		String command = String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName());
		return new String[] { command };
	}

}
