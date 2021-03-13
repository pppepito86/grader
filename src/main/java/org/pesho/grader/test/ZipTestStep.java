package org.pesho.grader.test;

import java.io.File;

public class ZipTestStep extends TestStep {

	public static final String BINARY_FILE_ENDING = ".zip";

	public static final String EXECUTE_COMMAND_PATTERN = "/usr/bin/unzip -p /shared/%s %s";

	public ZipTestStep(File binaryFile, File inputFile, File outputFile, double time, int memory) {
		super(binaryFile, inputFile, outputFile, time, memory);
	}

	@Override
	public String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName(), inputFile.getName().replace("in", "out"));
	}

}
