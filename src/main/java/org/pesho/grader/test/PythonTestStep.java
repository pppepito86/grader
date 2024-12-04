package org.pesho.grader.test;

import java.io.File;

public class PythonTestStep extends TestStep {

	public static final String EXECUTE_COMMAND_PATTERN = "/usr/bin/pypy3 ./%s";

	public PythonTestStep(File binaryFile, File managerFile, File piperFile, File inputFile, File outputFile, double time, int memory, int processes, int openFiles) {
		super(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, processes, openFiles);
	}

	@Override
	public String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName());
	}

}
