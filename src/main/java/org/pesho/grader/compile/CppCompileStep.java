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
	public static final String COMPILE_CPP14_COMMAND_PATTERN = "g++ -O2 -std=c++14 -o %s %s";
	public static final String COMPILE_CPP17_COMMAND_PATTERN = "g++ -O2 -std=c++17 -o %s %s";
	public static final String COMPILE_CPP98_COMMAND_PATTERN = "g++ -O2 -std=c++98 -o %s %s";
	public static final String SOURCE_FILE_ENDING = ".cpp";

	public CppCompileStep(File sourceFile, File graderDir) {
		super(sourceFile, graderDir);
	}
	
	@Override
	public void execute() {
		super.execute();
		if (getVerdict() != Verdict.OK) {
			System.out.println("Compilation failed with c++11, will try without it.");
			tryOther(COMPILE_NO_CPP11_COMMAND_PATTERN);
		}
		if (getVerdict() != Verdict.OK) {
			System.out.println("Compilation failed, will try with c++14.");
			tryOther(COMPILE_CPP14_COMMAND_PATTERN);
		}
		if (getVerdict() != Verdict.OK) {
			System.out.println("Compilation failed, will try with c++17.");
			tryOther(COMPILE_CPP17_COMMAND_PATTERN);
		}
		if (getVerdict() != Verdict.OK) {
			System.out.println("Compilation failed, will try with c++98.");
			tryOther(COMPILE_CPP98_COMMAND_PATTERN);
		}
	}

	private void tryOther(String pattern) {
		String compiledFileName = getBinaryFileName();
		String command = String.format(pattern, compiledFileName, sourceFile.getName());
		
		try {
			createSandboxDirectory();
			copySandboxInput();
	
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
		} catch (Exception e) {
		}
	}

	@Override
	public String[] getCommands() {
		String compiledFileName = getBinaryFileName();
		String command = String.format(COMPILE_COMMAND_PATTERN, compiledFileName, getAllFiles());
		return new String[] { command };
	}
	
	private String getAllFiles() {
		String files = sourceFile.getName();
		if (graderDir.exists()) {
			for (File file: graderDir.listFiles()) files += " " + file.getName();
		}
		return files;
	}
	
	
	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", "");
	}

}
