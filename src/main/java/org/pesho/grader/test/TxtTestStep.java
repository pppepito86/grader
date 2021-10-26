package org.pesho.grader.test;

import java.io.File;

public class TxtTestStep extends TestStep {

	public static final String BINARY_FILE_ENDING = ".txt";

	public static final String EXECUTE_COMMAND_PATTERN = "/bin/cat %s";

	public TxtTestStep(File binaryFile, File graderFile, File inputFile, File outputFile, double time, int memory) {
		super(binaryFile, graderFile, inputFile, outputFile, time, memory);
	}

	@Override
	public String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName());
	}

}
