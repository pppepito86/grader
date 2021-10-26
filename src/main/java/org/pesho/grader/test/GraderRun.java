package org.pesho.grader.test;

import java.io.File;

import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.SandboxExecutor;

public class GraderRun extends Thread {
	
	public static final String EXECUTE_COMMAND_PATTERN = "./%s";

	protected final File graderFile;
	protected final File inputFile;
	protected final File outputFile;
	protected final File sandboxDir;
	protected final double time;
	protected final int memory;
	protected StepResult result;

	public GraderRun(File graderFile, File inputFile, File outputFile, double time, int memory, File sandboxDir) {
		this.graderFile = graderFile.getAbsoluteFile();
		this.inputFile = inputFile.getAbsoluteFile();
		this.outputFile = outputFile.getAbsoluteFile();
		this.time = time;
		this.memory = memory;
		this.sandboxDir = sandboxDir;
	}

	public void run() {
		try {
			CommandResult commandResult = new SandboxExecutor()
					.name(1)
					.directory(sandboxDir)
					.input(inputFile.getName())
					.output(outputFile.getName())
					.error("error1")
					.timeout(time)
					.ioTimeout(getIoTimeout())
					.trusted(true)
					.memory(memory)
					.extraMemory(5)
					.command(getCommand()).execute().getResult();
			
			result = getResult(commandResult);
		} catch (Exception e) {
			e.printStackTrace();
			result = new StepResult(Verdict.SE, result.getReason(), result.getExitCode());
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
	
	public synchronized StepResult getResult() {
		return result;
	}
	
	public Verdict getVerdict() {
		return result.getVerdict();
	}

	protected String getCommand() {
		return String.format(EXECUTE_COMMAND_PATTERN, graderFile.getName());
	}
	
	public double getIoTimeout() {
		return 0;
	}
	
}
