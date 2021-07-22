package org.pesho.grader.compile;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.SandboxExecutor;

public class CppCompileStep extends CompileStep {

	public static final String COMPILE_NO_CPP11_COMMAND_PATTERN = "/usr/bin/g++ -DEVAL -O2 -pipe -static -s -o %s ./%s";
	public static final String COMPILE_CPP_11_COMMAND_PATTERN = "/usr/bin/g++ -DEVAL -std=c++11 -O2 -pipe -static -s -o %s ./%s";
	public static final String COMPILE_CPP14_COMMAND_PATTERN = "/usr/bin/g++ -DEVAL -std=c++14 -O2 -pipe -static -s -o %s ./%s";
	public static final String COMPILE_CPP17_COMMAND_PATTERN = "/usr/bin/g++ -DEVAL -std=c++17 -O2 -pipe -static -s -o %s ./%s";
	public static final String COMPILE_CPP98_COMMAND_PATTERN = "/usr/bin/g++ -DEVAL -std=c++98 -O2 -pipe -static -s -o %s ./%s";
	public static final String SOURCE_FILE_ENDING = ".cpp";

	public CppCompileStep(File sourceFile) {
		this(sourceFile, null);
	}
	
	public CppCompileStep(File sourceFile, File graderDir) {
		super(sourceFile, graderDir);
	}
	
	@Override
	public void execute() {
		super.execute();
		if (getVerdict() != Verdict.OK) {
			System.out.println("Compilation failed with c++17, will try wit c++11.");
			tryOther(COMPILE_CPP_11_COMMAND_PATTERN);
		}
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
		String[] command = getCommands(pattern);
		
		try {
			createSandboxDirectory();
			copySandboxInput();
	
			CommandResult commandResult = new SandboxExecutor()
					.directory(sandboxDir)
					.trusted(true)
					.showError()
					.timeout(10)
					.memory(256)
					.command(command[0])
					.execute().getResult();
			StepResult result = getResult(commandResult);
			if (result.getVerdict() == Verdict.OK) {
				this.result = result;
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
		return getCommands(COMPILE_CPP17_COMMAND_PATTERN);
	}
	
	public String[] getCommands(String pattern) {
		String compiledFileName = getBinaryFileName();
		String command = String.format(pattern, compiledFileName, getAllFiles());
		return new String[] { command };
	}
	
	private String getAllFiles() {
		String files = sourceFile.getName();
		if (graderDir != null && graderDir.exists()) {
			for (File file: graderDir.listFiles()) {
				if (!file.isFile()) continue;
				
				files += " " + file.getName();
			}
		}
		return files;
	}
	
	
	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", "");
	}

}
