package org.pesho.grader.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Precision;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.SandboxExecutor;
import org.zeroturnaround.exec.ProcessExecutor;

public abstract class TestStep implements BaseStep {

	protected final File binaryFile;
	protected final File managerFile;
	protected final File inputFile;
	protected final File outputFile;
	protected final File sandboxDir;
	protected final double time;
	protected final int memory;
	protected final int processes;
	protected StepResult result;

	public TestStep(File binaryFile, File managerFile, File inputFile, File outputFile, double time, int memory, int processes) {
		this.binaryFile = binaryFile.getAbsoluteFile();
		this.managerFile = managerFile != null ? managerFile.getAbsoluteFile():null;
		this.inputFile = inputFile.getAbsoluteFile();
		this.outputFile = outputFile.getAbsoluteFile();
		this.time = time;
		this.memory = memory;
		this.processes = processes;
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
					.ioTimeout(getIoTimeout())
					.trusted(this instanceof JavaTestStep)
					.memory(memory)
					.processes((this instanceof JavaTestStep)?100:(managerFile!=null?processes+2:processes))
					.extraMemory((this instanceof JavaTestStep)?0:5)
					.command(managerFile != null ? getPiperCommand() : getCommand()).execute().getResult();
			result = getResult(commandResult);
			
			copySandboxOutput();
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
	
	public String getPiperCommand() {
		String pattern = "./piper %d %s %s";
		return String.format(pattern, processes, managerFile.getName(), binaryFile.getName());
	}
	
	protected void createSandboxDirectory() {
		sandboxDir.mkdirs();
	}
	
	protected void copySandboxInput() throws Exception {
		FileUtils.copyFile(binaryFile, new File(sandboxDir, binaryFile.getName()));
		new File(sandboxDir, binaryFile.getName()).setExecutable(true);
		new ProcessExecutor().command("chmod", "+x", new File(sandboxDir, binaryFile.getName()).getAbsolutePath()).execute();
		FileUtils.copyFile(inputFile, new File(sandboxDir, inputFile.getName()));
		
		if (managerFile != null) {
			FileUtils.copyFile(managerFile, new File(sandboxDir, managerFile.getName()));
			new File(sandboxDir, managerFile.getName()).setExecutable(true);
			new ProcessExecutor().command("chmod", "+x", new File(sandboxDir, managerFile.getName()).getAbsolutePath()).execute();
			
			File piperFile = new File("/vagrant/worker/piper/piper");
			FileUtils.copyFile(piperFile, new File(sandboxDir, piperFile.getName()));
			new File(sandboxDir, piperFile.getName()).setExecutable(true);
			new ProcessExecutor().command("chmod", "+x", new File(sandboxDir, piperFile.getName()).getAbsolutePath()).execute();
		}
	}
	
	protected void copySandboxOutput() throws IOException {
		FileUtils.copyFile(new File(sandboxDir, outputFile.getName()), outputFile);
	}
	
	public double getIoTimeout() {
		return 0;
	}
	
}
