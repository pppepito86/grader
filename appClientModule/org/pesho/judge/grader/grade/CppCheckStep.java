package org.pesho.judge.grader.grade;

import java.io.File;

public class CppCheckStep extends CheckStep {

	public static final String GRADE_COMMAND_PATTERN = "./%s %s %s %s";

	public CppCheckStep(File binaryFile, File inputFile, File outputFile, File solutionFile) {
		super(binaryFile, inputFile, outputFile, solutionFile);
	}

	@Override
	public String[] getCommands() {
		String command = String.format(GRADE_COMMAND_PATTERN, binaryFile.getName(), inputFile.getName(),
				outputFile.getName(), solutionFile.getName());
		return new String[] { command };
	}

}
