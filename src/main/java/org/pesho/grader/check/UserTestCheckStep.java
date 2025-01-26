package org.pesho.grader.check;

import java.io.File;

public class UserTestCheckStep extends CheckStep {

	public static final String GRADE_COMMAND_PATTERN = "";
	
	public UserTestCheckStep(File binaryFile, File inputFile, File solutionFile) {
		super(binaryFile, inputFile, null, solutionFile);
	}

	public void execute() {
		result = getPartialResult(1, null);
	}

	protected String getCommand() {
		return String.format(GRADE_COMMAND_PATTERN);
	}

}
