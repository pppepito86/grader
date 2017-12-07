package org.pesho.grader.check;

import java.io.File;

public class CppCheckStep extends CheckStep {

	public static final String GRADE_COMMAND_PATTERN = "./%s %s %s %s";

	public CppCheckStep(File binaryFile, File inputFile, File outputFile, File solutionFile) {
		super(binaryFile, inputFile, outputFile, solutionFile);
	}

	@Override
	public String getCommand() {
		return String.format(GRADE_COMMAND_PATTERN, binaryFile.getName(), inputFile.getName(),
				outputFile.getName(), solutionFile.getName());
	}

}
