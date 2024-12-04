package org.pesho.grader.check;

import java.io.File;

public class PythonCheckStep extends CheckStep {

	public static final String GRADE_COMMAND_PATTERN = "/usr/bin/pypy3 %s %s %s %s";

	public PythonCheckStep(File binaryFile, File inputFile, File outputFile, File solutionFile) {
		super(binaryFile, inputFile, outputFile, solutionFile);
	}

	@Override
	public String getCommand() {
		return String.format(GRADE_COMMAND_PATTERN, binaryFile.getName(), inputFile.getName(),
				outputFile.getName(), solutionFile.getName());
	}

}
