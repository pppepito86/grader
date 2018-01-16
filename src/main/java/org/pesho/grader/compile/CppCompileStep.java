package org.pesho.grader.compile;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.SandboxExecutor;

public class CppCompileStep extends CompileStep {

	public static final String COMPILE_COMMAND_PATTERN = "g++ -O2 -std=c++11 -o %s %s";
	public static final String COMPILE_NO_CPP11_COMMAND_PATTERN = "g++ -O2 -o %s %s";
	public static final String SOURCE_FILE_ENDING = ".cpp";

	public CppCompileStep(File sourceFile) {
		super(sourceFile);
	}
	
	@Override
	public void execute() {
		super.execute();
		if (getVerdict() != Verdict.OK) {
			System.out.println("Compilation failed with c++11, will try without it.");
			tryWithNoCpp11();
		}
	}

	private void tryWithNoCpp11() {
		String compiledFileName = getBinaryFileName();
		String command = String.format(COMPILE_COMMAND_PATTERN, compiledFileName, sourceFile.getName());

		CommandResult commandResult = new SandboxExecutor()
				.directory(sandboxDir)
				.command(command)
				.execute().getResult();
		StepResult result = getResult(commandResult);
		if (result.getVerdict() == Verdict.OK) {
			try {
				copySandboxOutput();
				this.result = result;
			} catch (Exception e) {
				e.printStackTrace();
				this.result = new StepResult(Verdict.SE, e.getMessage());
			} finally {
				FileUtils.deleteQuietly(sandboxDir);
			}
		}
	}

	@Override
	public String[] getCommands() {
		String compiledFileName = getBinaryFileName();
		String command = String.format(COMPILE_COMMAND_PATTERN, compiledFileName, sourceFile.getName());
		return new String[] { command };
	}
	
	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", "");
	}

}
