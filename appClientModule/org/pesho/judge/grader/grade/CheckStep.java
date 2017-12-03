package org.pesho.judge.grader.grade;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.pesho.judge.SandboxExecutor;
import org.pesho.judge.SandboxResult;
import org.pesho.judge.grader.step.BaseStep;

public abstract class CheckStep implements BaseStep {
	
	protected final File binaryFile;
	protected final File inputFile;
	protected final File outputFile;
	protected final File solutionFile;
	
	public CheckStep(File binaryFile, File inputFile, File outputFile, File solutionFile) {
		this.binaryFile = binaryFile;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.solutionFile = solutionFile;
	}
	
	public double execute() {
		System.out.println("*********");
		System.out.println("*Grading*");
		File sandboxDir = new File(binaryFile.getParentFile(), "sandbox_" + outputFile.getName());
		sandboxDir.mkdirs();
		try {
			System.out.println(" - sandbox " + sandboxDir.getAbsolutePath());
			System.out.println(" - copying " + binaryFile.getAbsolutePath());
			FileUtils.copyFile(binaryFile, new File(sandboxDir, binaryFile.getName()));
			System.out.println(" - copying " + inputFile.getAbsolutePath());
			FileUtils.copyFile(inputFile, new File(sandboxDir, inputFile.getName()));
			System.out.println(" - copying " + outputFile.getAbsolutePath());
			FileUtils.copyFile(outputFile, new File(sandboxDir, outputFile.getName()));
			System.out.println(" - copying " + solutionFile.getAbsolutePath());
			FileUtils.copyFile(solutionFile, new File(sandboxDir, solutionFile.getName()));
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		
		System.out.println(" -input " + inputFile.getAbsolutePath());
		System.out.println(" -output " + outputFile.getAbsolutePath());
		System.out.println(" -solution " + solutionFile.getAbsolutePath());
		String[] commands = getCommands();
		for (String command: commands) {
			SandboxResult result = new SandboxExecutor()
					.directory(sandboxDir)
					.input("/dev/null")
					.output("grade_" + inputFile.getName())
					.command(command)
					.execute();
			System.out.println("Executing command: " + command);
			System.out.println("Execution finished with result: " + result.getResult());
		}
		File gradeFile = new File(sandboxDir, "grade_" + inputFile.getName());
		String grade;
		try {
			grade = FileUtils.readFileToString(gradeFile).trim();
			System.out.println(" - grade is " + grade);
			return Double.valueOf(grade);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	protected abstract String[] getCommands();
	
}
