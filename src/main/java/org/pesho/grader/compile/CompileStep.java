package org.pesho.grader.compile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.SandboxExecutor;

public abstract class CompileStep implements BaseStep {

	protected final File sourceFile;
	protected final File sandboxDir;
	protected StepResult result;

	public CompileStep(File sourceFile) {
		this.sourceFile = sourceFile.getAbsoluteFile();
		this.sandboxDir = new File(sourceFile.getParentFile(), "sandbox_compile");
	}

	public void execute() {
		try {
			createSandboxDirectory();
			copySandboxInput();

			StepResult result = Arrays.stream(getCommands())
				.map(command -> new SandboxExecutor()
						.directory(sandboxDir)
						.command(command)
						.execute().getResult())
				.map(x -> getResult(x))
				.filter(x -> x.getVerdict() != Verdict.OK)
				.findFirst().orElse(new StepResult(Verdict.OK));
			copySandboxOutput();
			
			this.result = result;
		} catch (Exception e) {
			e.printStackTrace();
			this.result = new StepResult(Verdict.SE, e.getMessage());
		} finally {
			FileUtils.deleteQuietly(sandboxDir);
		}
	}

	@Override
	public StepResult getResult() {
		return result;
	}
	
	@Override
	public Verdict getVerdict() {
		return getResult().getVerdict();
	}
	
	private StepResult getResult(CommandResult result) {
		switch (result.getStatus()) {
		case SUCCESS: return new StepResult(Verdict.OK);
		case SYSTEM_ERROR: return new StepResult(Verdict.SE, result.getReason());
		default: return new StepResult(Verdict.CE, result.getStatus() + " " + result.getReason());
		}
	}

	public File getBinaryFile() {
		return new File(sourceFile.getParentFile(), getBinaryFileName());
	}

	public abstract String getBinaryFileName();

	protected abstract String[] getCommands();

	private void createSandboxDirectory() {
		sandboxDir.mkdirs();
	}

	private void copySandboxInput() throws IOException {
		FileUtils.copyFile(sourceFile, new File(sandboxDir, sourceFile.getName()));
	}

	private void copySandboxOutput() throws IOException {
		File sandboxBinaryFile = new File(sandboxDir, getBinaryFileName());
		if (!sandboxBinaryFile.exists()) return;
		
		FileUtils.copyFile(sandboxBinaryFile, getBinaryFile());
		getBinaryFile().setExecutable(true);
	}

}
