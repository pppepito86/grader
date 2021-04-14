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
	protected final File graderDir;
	protected final File sandboxDir;
	protected StepResult result;

	public CompileStep(File sourceFile, File graderDir) {
		this.sourceFile = sourceFile.getAbsoluteFile();
		this.graderDir = graderDir;
		this.sandboxDir = new File(sourceFile.getParentFile(), "sandbox_compile");
	}

	public void execute() {
		try {
			createSandboxDirectory();
			copySandboxInput();
			copyGraderFiles();

			StepResult result = Arrays.stream(getCommands())
				.map(command -> buildCommand(command)
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
//			FileUtils.deleteQuietly(sandboxDir);
		}
	}
	
	private SandboxExecutor buildCommand(String command) {
		Integer memory = 256;
		if (this instanceof JavaCompileStep) {
//			memory = null;
		}
		return new SandboxExecutor()
				.directory(sandboxDir)
				.trusted(true)
				.showError()
				.timeout(10)
				.memory(memory)
				.command(command);		
	}

	@Override
	public StepResult getResult() {
		return result;
	}
	
	@Override
	public Verdict getVerdict() {
		return getResult().getVerdict();
	}
	
	protected StepResult getResult(CommandResult result) {
		switch (result.getStatus()) {
		case SUCCESS: return new StepResult(Verdict.OK);
		case TIMEOUT: return new StepResult(Verdict.CE, "Compilation TL");
		case OOM: return new StepResult(Verdict.CE, "Compilation ML");
		case SYSTEM_ERROR: return new StepResult(Verdict.SE, result.getReason());
		default: return new StepResult(Verdict.CE, result.getReason());
		}
	}

	public File getBinaryFile() {
		return new File(sourceFile.getParentFile(), getBinaryFileName());
	}

	public abstract String getBinaryFileName();

	protected abstract String[] getCommands();

	protected void createSandboxDirectory() {
		sandboxDir.mkdirs();
	}

	protected void copySandboxInput() throws IOException {
		FileUtils.copyFile(sourceFile, new File(sandboxDir, sourceFile.getName()));
	}

	protected void copyGraderFiles() throws IOException {
		if (graderDir == null || !graderDir.exists()) return;
		for (File file: graderDir.listFiles()) {
			File newFile = new File(sandboxDir, file.getName());
			FileUtils.copyFile(file, newFile);
		}
	}
	
	protected void copySandboxOutput() throws IOException {
		File sandboxBinaryFile = new File(sandboxDir, getBinaryFileName());
		if (!sandboxBinaryFile.exists()) return;
		
		FileUtils.copyFile(sandboxBinaryFile, getBinaryFile());
		getBinaryFile().setExecutable(true);
	}

}
