package org.pesho.grader.test;

import java.io.File;

public class JavaTestStep extends TestStep {

	public static final String EXECUTE_COMMAND_PATTERN = "/usr/bin/java -jar %s";

	public JavaTestStep(File binaryFile, File inputFile, File outputFile, double time, int memory) {
		super(binaryFile, inputFile, outputFile, time, memory);
	}

	@Override
	public String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName());
	}

}
