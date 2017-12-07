package org.pesho.grader.compile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandStatus;
import org.pesho.sandbox.SandboxExecutor;

public abstract class CompileStep implements BaseStep {

	protected final File sourceFile;
	protected final File sandboxDir;
	Verdict verdict;

	public CompileStep(File sourceFile) {
		this.sourceFile = sourceFile.getAbsoluteFile();
		this.sandboxDir = new File(sourceFile.getParentFile(), "sandbox_compile");
	}

	public double execute() {
		try {
			createSandboxDirectory();
			copySandboxInput();

			Verdict verdict = Arrays.stream(getCommands())
				.map(command -> new SandboxExecutor().directory(sandboxDir).command(command).execute().getStatus())
				.filter(status -> status != CommandStatus.SUCCESS)
				.map(status -> getVerdict(status))
				.findFirst().orElse(Verdict.OK);
			copySandboxOutput();
			
			this.verdict = verdict;
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

	@Override
	public Verdict getVerdict() {
		return verdict;
	}
	
	private Verdict getVerdict(CommandStatus status) {
		switch (status) {
		case SUCCESS: return Verdict.OK;
		case SYSTEM_ERROR: return Verdict.SE;
		default: return Verdict.CE;
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
		FileUtils.copyFile(new File(sandboxDir, getBinaryFileName()), getBinaryFile());
		getBinaryFile().setExecutable(true);
	}

}
