package org.pesho.grader.test;

import java.io.File;

public class CppTestStep extends TestStep {

	public static final String EXECUTE_COMMAND_PATTERN = "./%s";
	
	private boolean isInteractive;

	public CppTestStep(File binaryFile, File inputFile, File outputFile, double time, int memory, boolean isInteractive) {
		super(binaryFile, inputFile, outputFile, time, memory);
		this.isInteractive = isInteractive;
	}

	@Override
	public String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, binaryFile.getName());
	}
	
	@Override
	public boolean useExtraMetadata() {
		return isInteractive;
	}

}
