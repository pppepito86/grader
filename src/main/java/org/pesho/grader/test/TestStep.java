package org.pesho.grader.test;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandStatus;
import org.pesho.sandbox.SandboxExecutor;
import org.pesho.sandbox.SandboxResult;

public abstract class TestStep implements BaseStep {
	
	protected final File binaryFile;
	protected final File inputFile;
	protected final File outputFile;
	Verdict verdict;
	
	public TestStep(File binaryFile, File inputFile, File outputFile) {
		this.binaryFile = binaryFile.getAbsoluteFile();
		this.inputFile = inputFile.getAbsoluteFile();
		this.outputFile = outputFile.getAbsoluteFile();
	}
	
	public double execute() {
		//System.out.println("**********");
		//System.out.println("*Testing* " + inputFile.getAbsolutePath());
		File sandboxDir = new File(binaryFile.getParentFile(), "sandbox_" + inputFile.getName());
		sandboxDir.mkdirs();
		try {
			//System.out.println(" - sandbox " + sandboxDir.getAbsolutePath());
			//System.out.println(" - copying " + binaryFile.getAbsolutePath());
			FileUtils.copyFile(binaryFile, new File(sandboxDir, binaryFile.getName()));
			//System.out.println(" - copying " + inputFile.getAbsolutePath());
			FileUtils.copyFile(inputFile, new File(sandboxDir, inputFile.getName()));
		} catch (Exception e) {
			verdict = Verdict.SE;
			e.printStackTrace();
			return 0;
		}
		
//		System.out.println(" - testing");
		String[] commands = getCommands();
		for (String command: commands) {
			SandboxResult result = new SandboxExecutor()
					.directory(sandboxDir)
					.input(inputFile.getName())
					.output(outputFile.getName())
					.timeout(1.0)
					.command(command)
					.execute();
			//System.out.println("Executing command: " + command);
			//System.out.println("Execution finished| with result: " + result.getResult());
			//System.out.println(" - status " + result.getStatus());
			if (result.getStatus() == CommandStatus.OOM) verdict = Verdict.ML;
			else if (result.getStatus() == CommandStatus.PROGRAM_ERROR) verdict = Verdict.RE;
			else if (result.getStatus() == CommandStatus.TIMEOUT) verdict = Verdict.TL;
			else if (result.getStatus() != CommandStatus.SUCCESS) verdict = Verdict.SE;
			if (result.getStatus() != CommandStatus.SUCCESS) {
				return 0;
			}
		}
		
		try {
			FileUtils.copyFile(new File(sandboxDir, outputFile.getName()), outputFile);
			//System.out.println(" - copying " + outputFile.getAbsolutePath());
		} catch (Exception e) {
			verdict = Verdict.SE;
			e.printStackTrace();
			return 0;
		}
		verdict = Verdict.OK;
		return 1.0;
	}
	
	@Override
	public Verdict getVerdict() {
		return verdict;
	}
	
	protected abstract String[] getCommands();
	
}
