package org.pesho.grader.check;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.CommandStatus;
import org.pesho.sandbox.SandboxExecutor;
import org.zeroturnaround.exec.ProcessExecutor;

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
		this.sandboxDir = new File(binaryFile != null ? binaryFile.getParentFile() : solutionFile.getParentFile(), "sandbox_" + inputFile.getName());
	}

	public void execute() {
		try {
			createSandboxDirectory();
			copySandboxInput();
			CommandResult statusResult = new SandboxExecutor()
					.directory(sandboxDir)
					.output("grade_" + inputFile.getName())
					.error("grade_err_" + inputFile.getName())
					.trusted(true)
					.showError()
					.command(getCommand())
					.execute().getResult();
			
			result = getResult(statusResult);
		} catch (Exception e) {
			e.printStackTrace();
			result = new StepResult(Verdict.SE, e.getMessage());
		} finally {
			FileUtils.deleteQuietly(sandboxDir);
		}
	}

	private StepResult getResult(CommandResult result) throws IOException {
		if (result.getStatus() != CommandStatus.SUCCESS) {
			return new StepResult(Verdict.SE, result.getReason());
		}
		File gradeFile = new File(sandboxDir, "grade_" + inputFile.getName());
		String gradeString = FileUtils.readLines(gradeFile, StandardCharsets.UTF_8.toString()).get(0).trim();

		File errorFile = new File(sandboxDir, "grade_err_" + inputFile.getName());
		String errorString = "";
		if (errorFile.exists()) errorString = FileUtils.readFileToString(errorFile, StandardCharsets.UTF_8.toString());
		
		return getPartialResult(gradeString, errorString);
	}
	
	protected StepResult getPartialResult(String scoreString, String reason) {
		double score = Double.valueOf(scoreString);
		String output = readOutput(outputFile);
		String solution = readOutput(solutionFile);
		if (scoreString.startsWith("1") && Double.compare(score, 1.0) == 0) {
			return new StepResult(Verdict.OK, reason, solution, output, 1.);
		}
		if (scoreString.equals("0")) {
			return new StepResult(Verdict.WA, reason, solution, output, 0.);
		}

		return new StepResult(Verdict.PARTIAL, reason, solution, output, score);
	}
	
	protected String readOutput(File file) {
		if (file == null) return "";
	    try (InputStream is = new FileInputStream(file)) {
	    	String output = "";
	    	byte[] b = new byte[1021];
	        int read = is.read(b);
	        if (read > 0) output += new String(b, 0, read);
	        if (is.available() > 0) {
	        	output += "...";
	        }
	        return output;
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

	private void copySandboxInput() throws Exception {
		FileUtils.copyFile(binaryFile, new File(sandboxDir, binaryFile.getName()));
		new File(sandboxDir, binaryFile.getName()).setExecutable(true);
		new ProcessExecutor().command("chmod", "+x", new File(sandboxDir, binaryFile.getName()).getAbsolutePath()).execute();
		FileUtils.copyFile(inputFile, new File(sandboxDir, inputFile.getName()));
		if (outputFile != null) FileUtils.copyFile(outputFile, new File(sandboxDir, outputFile.getName()));
		FileUtils.copyFile(solutionFile, new File(sandboxDir, solutionFile.getName()));

	}

}
