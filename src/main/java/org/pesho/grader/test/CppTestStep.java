package org.pesho.grader.test;

import java.io.File;

public class CppTestStep extends TestStep {

	public static final String EXECUTE_COMMAND_PATTERN = "./%s";
	
	private double ioTime;

	public CppTestStep(File binaryFile, File managerFile, File piperFile, File inputFile, File outputFile, double time, int memory, int processes, int openFiles, double ioTime) {
		super(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, processes, openFiles);
		this.ioTime = ioTime;
	}

	@Override
	public String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName());
	}
	
	@Override
	public double getIoTimeout() {
		return ioTime;
	}

}
