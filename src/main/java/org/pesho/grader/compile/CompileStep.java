package org.pesho.grader.compile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.MinimalEtc;
import org.pesho.sandbox.SandboxExecutor;

public abstract class CompileStep implements BaseStep {

	protected final File sourceFile;
	protected final File graderDir;
	protected final File sandboxDir;
	protected final Map<String, Double> time;
	protected final Map<String, Integer> memory;
	protected StepResult result;

	public CompileStep(File sourceFile, File graderDir, Map<String, Double> time, Map<String, Integer> memory) {
		this.sourceFile = sourceFile.getAbsoluteFile();
		this.graderDir = graderDir;
		this.sandboxDir = new File(sourceFile.getParentFile(), "sandbox_compile");
		this.time = time;
		this.memory = memory;
	}

	public void execute() {
		try {
			createSandboxDirectory();
			copySandboxInput();
			copyGraderFiles();

			List<StepResult> results = Arrays.stream(getCommands())
				.map(command -> buildCommand(command)
						.execute().getResult())
				.map(x -> getResult(x))
				.collect(Collectors.toList());
			StepResult result;
			if (results.stream().anyMatch(x -> x.getVerdict() != Verdict.OK)) {
				result = results.stream().filter(x -> x.getVerdict() != Verdict.OK).findFirst().orElse(null);
			}
			else {
				if (results.size() == 1) result = results.get(0);
				else result = new StepResult(Verdict.OK);
			}
			copySandboxOutput();
			
			this.result = result;
		} catch (Exception e) {
			e.printStackTrace();
			this.result = new StepResult(Verdict.SE, e.getMessage());
		} finally {
			if (!(this instanceof ZipLaTexCompileStep)) FileUtils.deleteQuietly(sandboxDir);
		}
	}
	
	private SandboxExecutor buildCommand(String command) {
		if (command.equals(JavaCompileStep.JAR_COMMAND_PATTERN)) {
			command = String.format(JavaCompileStep.JAR_COMMAND_PATTERN, getBinaryFileName(), ((JavaCompileStep) this).getMainClassName());
		}
		
		double timeout = time.get("default");
		int maxMemory = memory.get("default");
		if (this instanceof JavaCompileStep || this instanceof JavaNativeImageCompileStep) timeout = time.get("java");
		if (this instanceof JavaCompileStep || this instanceof JavaNativeImageCompileStep) maxMemory = memory.get("java");
		if (this instanceof ZipLaTexCompileStep) timeout = 30;
		if (this instanceof ZipLaTexCompileStep) maxMemory = 1024;
		SandboxExecutor sandbox = new SandboxExecutor()
				.directory(sandboxDir)
				.trusted(true)
				.trustedDirectories(getTrustedDirectories())
				.showError()
				.timeout(timeout)
				.memory(maxMemory)
				.command(command);
		if (useMinimalEtc()) sandbox.etcDir(MinimalEtc.getDir());
		if (this instanceof PythonCompileStep || (this instanceof ZipLaTexCompileStep && !command.equals(ZipLaTexCompileStep.NOTEX_COMMAND_PATTERN))) return sandbox.outputIsError();
		return sandbox;
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
		case SUCCESS: return new StepResult(Verdict.OK, result.getReason(), result.getExitCode(), result.getTime(), result.getMemory());
		case TIMEOUT: return new StepResult(Verdict.CE, "Compilation TL.\n" + result.getReason(), result.getExitCode(), result.getTime(), result.getMemory());
		case OOM: return new StepResult(Verdict.CE, "Compilation ML.\n" + result.getReason(), result.getExitCode(), result.getTime(), result.getMemory());
		case SYSTEM_ERROR: return new StepResult(Verdict.SE, result.getReason(), result.getExitCode());
		default: return new StepResult(Verdict.CE, result.getReason(), result.getExitCode(), result.getTime(), result.getMemory());
		}
	}

	protected String getAllFiles() {
		String files = sourceFile.getName();
		if (graderDir != null && graderDir.exists()) {
			for (File file: graderDir.listFiles()) {
				if (!file.isFile()) continue;
				if (file.getName().equalsIgnoreCase("grader")) continue;
				if (file.getName().equals(sourceFile.getName())) continue;
				if (file.getName().equalsIgnoreCase(".DS_Store")) continue;
				
				files += " " + file.getName();
			}
		}
		return files;
	}

	public File getBinaryFile() {
		return new File(sourceFile.getParentFile(), getBinaryFileName());
	}

	public abstract String getBinaryFileName();

	protected abstract String[] getCommands();

	protected List<String> getTrustedDirectories() {
		return new ArrayList<>();
	}

	/**
	 * Whether to bind a curated /etc (see {@link MinimalEtc}) into the compile box instead of the
	 * host /etc. Enabled only for the C/C++ preprocessor languages, where a submission can leak host
	 * files via  #include "/etc/...". Other toolchains (mono needs /etc/mono, LaTeX needs /etc/fonts,
	 * the JVM/locale, ...) stay on the host /etc, so they are unaffected by this change.
	 */
	protected boolean useMinimalEtc() {
		return false;
	}

	protected void createSandboxDirectory() {
		sandboxDir.mkdirs();
	}

	protected void copySandboxInput() throws IOException {
		FileUtils.copyFile(sourceFile, new File(sandboxDir, sourceFile.getName()));
	}

	protected void copyGraderFiles() throws IOException {
		if (graderDir == null || !graderDir.exists()) return;
		for (File file: graderDir.listFiles()) {
			if (!file.isFile()) continue;
			
			File newFile = new File(sandboxDir, file.getName());
			if (newFile.exists()) continue;
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
