package org.pesho.grader.check;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.SandboxExecutor;

public abstract class CheckStep implements BaseStep {
	
	protected final File binaryFile;
	protected final File inputFile;
	protected final File outputFile;
	protected final File solutionFile;
	Verdict verdict;
	
	public CheckStep(File binaryFile, File inputFile, File outputFile, File solutionFile) {
		this.binaryFile = binaryFile;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.solutionFile = solutionFile;
	}
	
	public double execute() {
		//System.out.println("*********");
		//System.out.println("*Grading*");
		File sandboxDir = new File(binaryFile.getParentFile(), "sandbox_" + outputFile.getName());
		sandboxDir.mkdirs();
		try {
			//System.out.println(" - sandbox " + sandboxDir.getAbsolutePath());
			//System.out.println(" - copying " + binaryFile.getAbsolutePath());
			FileUtils.copyFile(binaryFile, new File(sandboxDir, binaryFile.getName()));
			new File(sandboxDir, binaryFile.getName()).setExecutable(true);
			//System.out.println(" - copying " + inputFile.getAbsolutePath());
			FileUtils.copyFile(inputFile, new File(sandboxDir, inputFile.getName()));
			//System.out.println(" - copying " + outputFile.getAbsolutePath());
			FileUtils.copyFile(outputFile, new File(sandboxDir, outputFile.getName()));
			//System.out.println(" - copying " + solutionFile.getAbsolutePath());
			FileUtils.copyFile(solutionFile, new File(sandboxDir, solutionFile.getName()));
		} catch (Exception e) {
			verdict = Verdict.SE;
			e.printStackTrace();
			return 0;
		}
		
//		System.out.println(" -input " + inputFile.getAbsolutePath());
//		System.out.println(" -output " + outputFile.getAbsolutePath());
//		System.out.println(" -solution " + solutionFile.getAbsolutePath());
		String[] commands = getCommands();
		for (String command: commands) {
			new SandboxExecutor()
					.directory(sandboxDir)
					.input("/dev/null")
					.output("grade_" + inputFile.getName())
					.command(command)
					.execute();
//			System.out.println("Executing command: " + command);
//			System.out.println("Execution finished with result: " + result.getResult());
		}
		File gradeFile = new File(sandboxDir, "grade_" + inputFile.getName());
		try {
			String gradeString = FileUtils.readFileToString(gradeFile).trim();
			double grade = Double.valueOf(gradeString);
			verdict = (grade==1.0)?Verdict.OK:Verdict.WA;
			return grade;
		} catch (IOException e) {
			verdict = Verdict.SE;
			e.printStackTrace();
			return 0;
		}
	}
	
	@Override
	public Verdict getVerdict() {
		return verdict;
	}
	
	protected abstract String[] getCommands();
	
}
