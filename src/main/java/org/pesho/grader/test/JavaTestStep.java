package org.pesho.grader.test;

import java.io.File;

public class JavaTestStep extends TestStep {

	public static final String EXECUTE_COMMAND_PATTERN = "java -jar %s";

	public JavaTestStep(File binaryFile, File inputFile, File outputFile) {
		super(binaryFile, inputFile, outputFile);
	}

	@Override
	public String[] getCommands() {
		String command = String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName());
		return new String[] { command };
	}

}
