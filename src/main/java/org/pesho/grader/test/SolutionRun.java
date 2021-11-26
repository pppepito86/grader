package org.pesho.grader.test;

import java.io.File;

import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandResult;
import org.pesho.sandbox.SandboxExecutor;

public class SolutionRun extends Thread {
	
	public static final String EXECUTE_COMMAND_PATTERN = "./%s";

	protected final String command;
	protected final File sandboxDir;
	protected final double time;
	protected final int memory;
	protected StepResult result;

	public SolutionRun(String command, double time, int memory, File sandboxDir) {
		this.command = command;
		this.time = time;
		this.memory = memory;
		this.sandboxDir = sandboxDir;
	}

	public void run() {
		try {
			CommandResult commandResult = new SandboxExecutor()
					.name(2)
					.directory(sandboxDir)
					.input("pipe_in2")
					.output("pipe_out2")
					.error("error2")
					.timeout(time)
					.ioTimeout(getIoTimeout())
					.trusted(false)
					.memory(memory)
					.extraMemory(5)
					.command(command).execute().getResult();

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

	public double getIoTimeout() {
		return 0;
	}
	
}
