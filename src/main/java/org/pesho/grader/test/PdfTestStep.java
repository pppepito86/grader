package org.pesho.grader.test;

import java.io.File;

public class PdfTestStep extends TestStep {

	public static final String BINARY_FILE_ENDING = ".pdf";

	public static final String EXECUTE_COMMAND_PATTERN = "/bin/cat";

	public PdfTestStep(File binaryFile, File managerFile, File piperFile, File inputFile, File outputFile, double time, int memory, int processes) {
		super(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, processes);
	}

	@Override
	public String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName());
	}

}
