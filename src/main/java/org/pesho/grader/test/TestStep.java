package org.pesho.grader.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.SandboxExecutor;
import org.zeroturnaround.exec.ProcessExecutor;

public abstract class TestStep implements BaseStep {

	protected final File binaryFile;
	protected final File inputFile;
	protected final File outputFile;
	protected final File sandboxDir;
	protected final double time;
	protected final int memory;
	protected StepResult result;

	public TestStep(File binaryFile, File inputFile, File outputFile, double time, int memory) {
		this.binaryFile = binaryFile.getAbsoluteFile();
		this.inputFile = inputFile.getAbsoluteFile();
		this.outputFile = outputFile.getAbsoluteFile();
		this.time = time;
		this.memory = memory;
		this.sandboxDir = new File(binaryFile.getParentFile(), "sandbox_" + inputFile.getName());
	}

	public void execute() {
		try {
			createSandboxDirectory();
			copySandboxInput();
			CommandResult commandResult = new SandboxExecutor()
					.directory(sandboxDir)
					.input(inputFile.getName())
					.output(outputFile.getName())
					.timeout(time)
					.useExtraMetadata(useExtraMetadata())
					.memory(this instanceof JavaTestStep ? null : memory)
					.command(getCommand()).execute().getResult();
			copySandboxOutput();
			
			result = getResult(commandResult);
		} catch (Exception e) {
			e.printStackTrace();
			result = new StepResult(Verdict.SE, result.getReason(), result.getExitCode());
		} finally {
			FileUtils.deleteQuietly(sandboxDir);
		}
	}

	protected StepResult getResult(CommandResult result) {
		switch (result.getStatus()) {
			case SUCCESS: return new StepResult(Verdict.OK, null, result.getExitCode(), result.getTime(), result.getMemory());
			case OOM: return new StepResult(Verdict.ML, result.getReason(), result.getExitCode(), result.getTime(), result.getMemory());
			case PROGRAM_ERROR:	return new StepResult(Verdict.RE, result.getReason(), result.getExitCode(), result.getTime(), result.getMemory());
			case TIMEOUT: return new StepResult(Verdict.TL, result.getReason(), result.getExitCode(), result.getTime(), result.getMemory());
			default:  return new StepResult(Verdict.SE, result.getReason(), result.getExitCode());
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
	}
	
	private void copySandboxOutput() throws IOException {
		FileUtils.copyFile(new File(sandboxDir, outputFile.getName()), outputFile);
	}
	
	public boolean useExtraMetadata() {
		return false;
	}
	
}
