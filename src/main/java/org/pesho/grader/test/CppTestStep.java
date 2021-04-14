package org.pesho.grader.test;

import java.io.File;

public class CppTestStep extends TestStep {

	public static final String EXECUTE_COMMAND_PATTERN = "./%s";
	
	private double ioTime;

	public CppTestStep(File binaryFile, File inputFile, File outputFile, double time, int memory, double ioTime) {
		super(binaryFile, inputFile, outputFile, time, memory);
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
