package org.pesho.grader.compile;
import java.io.File;

import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;

public class ZipCompileStep extends CompileStep {

	public static final String VALIDATE_COMMAND_PATTERN = "/usr/bin/unzip -t /shared/%s";

	public static final String SOURCE_FILE_ENDING = ".zip";

	public ZipCompileStep(File sourceFile, File graderDir) {
		super(sourceFile, graderDir);
	}

	@Override
	public String[] getCommands() {
		String validateCommand = String.format(VALIDATE_COMMAND_PATTERN, sourceFile.getName());
		return new String[] { validateCommand };
	}
	
	public String getBinaryFileName() {
		return sourceFile.getName();
	}
	
	protected StepResult getResult(CommandResult result) {
		switch (result.getStatus()) {
		case SUCCESS: return new StepResult(Verdict.OK);
		case TIMEOUT: return new StepResult(Verdict.CE, "Zip validation TL");
		case OOM: return new StepResult(Verdict.CE, "Zip validation ML");
		case SYSTEM_ERROR: return new StepResult(Verdict.SE, result.getReason());
		default: return new StepResult(Verdict.CE, "Not a valid zip file");
		}
	}

}
