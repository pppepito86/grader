package org.pesho.judge.grader.compile;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.pesho.judge.CommandStatus;
import org.pesho.judge.SandboxExecutor;
import org.pesho.judge.SandboxResult;
import org.pesho.judge.grader.step.BaseStep;

public abstract class CompileStep implements BaseStep {
	
	protected final File sourceFile;
	
	public CompileStep(File sourceFile) {
		this.sourceFile = sourceFile.getAbsoluteFile();
	}
	
	public double execute() {
		System.out.println("***********");
		System.out.println("*Compiling* " + sourceFile.getAbsolutePath());
		System.out.println(sourceFile.getParentFile());
		File sandboxDir = new File(sourceFile.getParentFile(), "sandbox_compile");
		sandboxDir.mkdirs();
		try {
			System.out.println(" - sandbox " + sandboxDir.getAbsolutePath());
			System.out.println(" - copying " + sourceFile.getAbsolutePath());
			FileUtils.copyFile(sourceFile, new File(sandboxDir, sourceFile.getName()));
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
				
		String[] commands = getCommands();
		for (String command: commands) {
			SandboxResult result = new SandboxExecutor()
					.directory(sandboxDir)
					.command(command)
					.execute();
			System.out.println("Executing command: " + command);
			System.out.println("Execution finished with result: " + result.getResult());
			System.out.println("Compilation status: " + result.getStatus());
			if (result.getStatus() != CommandStatus.SUCCESS) return 0;
		}
		
		try {
			System.out.println(new File(sandboxDir, getBinaryFileName()).getAbsolutePath());
			FileUtils.copyFile(new File(sandboxDir, getBinaryFileName()), getBinaryFile());
			System.out.println(" - copying " + getBinaryFile().getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	public File getBinaryFile() {
		return new File(sourceFile.getParentFile(), getBinaryFileName());
	}
	
	public abstract String getBinaryFileName();
	
	protected abstract String[] getCommands();
	
}
