package org.pesho.grader.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandStatus;
import org.pesho.sandbox.SandboxExecutor;

public abstract class TestStep implements BaseStep {

	protected final File binaryFile;
	protected final File inputFile;
	protected final File outputFile;
	protected final File sandboxDir;
	Verdict verdict;

	public TestStep(File binaryFile, File inputFile, File outputFile) {
		this.binaryFile = binaryFile.getAbsoluteFile();
		this.inputFile = inputFile.getAbsoluteFile();
		this.outputFile = outputFile.getAbsoluteFile();
		this.sandboxDir = new File(binaryFile.getParentFile(), "sandbox_" + inputFile.getName());
	}

	public double execute() {
		try {
			createSandboxDirectory();
			copySandboxInput();
			CommandStatus status = new SandboxExecutor().directory(sandboxDir).input(inputFile.getName())
					.output(outputFile.getName()).timeout(1.0).command(getCommand()).execute().getStatus();
			copySandboxOutput();
			
			verdict = getVerdict(status);
		} catch (Exception e) {
			e.printStackTrace();
			verdict = Verdict.SE;
		}
		if (verdict == Verdict.OK) {
			return 1.0;
		} else {
			return 0.0;
		}
	}

	private Verdict getVerdict(CommandStatus status) {
		switch (status) {
		case SUCCESS: return Verdict.OK;
		case OOM: return Verdict.ML;
		case PROGRAM_ERROR:	return Verdict.RE;
		case TIMEOUT: return Verdict.TL;
		default: return Verdict.SE;
		}
	}
	
	@Override
	public Verdict getVerdict() {
		return verdict;
	}

	protected abstract String getCommand();
	
	private void createSandboxDirectory() {
		sandboxDir.mkdirs();
	}
	
	private void copySandboxInput() throws IOException {
		FileUtils.copyFile(binaryFile, new File(sandboxDir, binaryFile.getName()));
		new File(sandboxDir, binaryFile.getName()).setExecutable(true);
		FileUtils.copyFile(inputFile, new File(sandboxDir, inputFile.getName()));
	}
	
	private void copySandboxOutput() throws IOException {
		FileUtils.copyFile(new File(sandboxDir, outputFile.getName()), outputFile);
	}
	
}
