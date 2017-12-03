package org.pesho.judge.grader.test;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.pesho.judge.CommandStatus;
import org.pesho.judge.SandboxExecutor;
import org.pesho.judge.SandboxResult;
import org.pesho.judge.grader.step.BaseStep;

public abstract class TestStep implements BaseStep {
	
	protected final File binaryFile;
	protected final File inputFile;
	protected final File outputFile;
	
	public TestStep(File binaryFile, File inputFile, File outputFile) {
		this.binaryFile = binaryFile.getAbsoluteFile();
		this.inputFile = inputFile.getAbsoluteFile();
		this.outputFile = outputFile.getAbsoluteFile();
	}
	
	public double execute() {
		System.out.println("**********");
		System.out.println("*Testing* " + inputFile.getAbsolutePath());
		File sandboxDir = new File(binaryFile.getParentFile(), "sandbox_" + inputFile.getName());
		sandboxDir.mkdirs();
		try {
			System.out.println(" - sandbox " + sandboxDir.getAbsolutePath());
			System.out.println(" - copying " + binaryFile.getAbsolutePath());
			FileUtils.copyFile(binaryFile, new File(sandboxDir, binaryFile.getName()));
			System.out.println(" - copying " + inputFile.getAbsolutePath());
			FileUtils.copyFile(inputFile, new File(sandboxDir, inputFile.getName()));
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		
		System.out.println(" - testing");
		String[] commands = getCommands();
		for (String command: commands) {
			SandboxResult result = new SandboxExecutor()
					.directory(sandboxDir)
					.input(inputFile.getName())
					.output(outputFile.getName())
					.timeout(1.0)
					.command(command)
					.execute();
			System.out.println("Executing command: " + command);
			System.out.println("Execution finished| with result: " + result.getResult());
			System.out.println(" - status " + result.getStatus());
			if (result.getStatus() != CommandStatus.SUCCESS) return 0;
		}
		
		try {
			FileUtils.copyFile(new File(sandboxDir, outputFile.getName()), outputFile);
			System.out.println(" - copying " + outputFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1.0;
	}
	
	protected abstract String[] getCommands();
	
}
