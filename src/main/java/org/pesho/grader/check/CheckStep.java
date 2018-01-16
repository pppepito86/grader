package org.pesho.grader.check;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.CommandStatus;
import org.pesho.sandbox.SandboxExecutor;

public abstract class CheckStep implements BaseStep {

	protected final File binaryFile;
	protected final File inputFile;
	protected final File outputFile;
	protected final File solutionFile;
	protected final File sandboxDir;
	protected StepResult result;

	public CheckStep(File binaryFile, File inputFile, File outputFile, File solutionFile) {
		this.binaryFile = binaryFile;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.solutionFile = solutionFile;
		this.sandboxDir = new File(binaryFile.getParentFile(), "sandbox_" + outputFile.getName());
	}

	public void execute() {
		try {
			createSandboxDirectory();
			copySandboxInput();
			CommandResult statusResult = new SandboxExecutor()
					.directory(sandboxDir)
					.input("/dev/null")
					.output("grade_" + inputFile.getName())
					.command(getCommand())
					.execute().getResult();

			result = getResult(statusResult);
		} catch (Exception e) {
			e.printStackTrace();
			result = new StepResult(Verdict.SE, result.getReason());
		} finally {
			FileUtils.deleteQuietly(sandboxDir);
		}
	}

	private StepResult getResult(CommandResult result) throws IOException {
		if (result.getStatus() != CommandStatus.SUCCESS) {
			return new StepResult(Verdict.SE, result.getReason());
		}
		File gradeFile = new File(sandboxDir, "grade_" + inputFile.getName());
		String gradeString = FileUtils.readFileToString(gradeFile).trim();
		double grade = Double.valueOf(gradeString);
		if (grade == 1.0) return new StepResult(Verdict.OK);
		else return new StepResult(Verdict.WA, readOutput());
	}
	
	protected String readOutput() {
	    try {
	    	byte[] b = new byte[1000];
	        InputStream is = new FileInputStream(solutionFile);
	        System.out.println("Solution file: " + solutionFile.exists());
	        int read = is.read(b);
	        System.out.println(new String(b));
	        is.close();
	        return new String(b, 0, read);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	@Override
	public StepResult getResult() {
		return result;
	}

	@Override
	public Verdict getVerdict() {
		return result.getVerdict();
	}

	protected abstract String getCommand();

	private void createSandboxDirectory() {
		sandboxDir.mkdirs();
	}

	private void copySandboxInput() throws IOException {
		FileUtils.copyFile(binaryFile, new File(sandboxDir, binaryFile.getName()));
		new File(sandboxDir, binaryFile.getName()).setExecutable(true);
		FileUtils.copyFile(inputFile, new File(sandboxDir, inputFile.getName()));
		FileUtils.copyFile(outputFile, new File(sandboxDir, outputFile.getName()));
		FileUtils.copyFile(solutionFile, new File(sandboxDir, solutionFile.getName()));

	}

}
