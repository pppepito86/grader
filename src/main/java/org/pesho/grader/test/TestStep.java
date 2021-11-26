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
	protected final File graderFile;
	protected final File inputFile;
	protected final File outputFile;
	protected final File sandboxDir;
	protected final double time;
	protected final int memory;
	protected StepResult result;

	public TestStep(File binaryFile, File graderFile, File inputFile, File outputFile, double time, int memory) {
		this.binaryFile = binaryFile.getAbsoluteFile();
		this.graderFile = graderFile != null ? graderFile.getAbsoluteFile():null;
		this.inputFile = inputFile.getAbsoluteFile();
		this.outputFile = outputFile.getAbsoluteFile();
		this.time = time;
		this.memory = memory;
		this.sandboxDir = new File(binaryFile.getParentFile(), "sandbox_" + inputFile.getName());
	}

	public void execute() {
		try {
			createSandboxDirectory();
			createPipes();
			copySandboxInput();
			
			GraderRun graderRun = null;
			SolutionRun solutionRun = null;
			if (graderFile != null) {
				graderRun = new GraderRun(graderFile, inputFile, outputFile, 3*time+1, memory, sandboxDir);
				graderRun.start();
				solutionRun = new SolutionRun(getCommand(), 3*time+1, memory, sandboxDir);
				solutionRun.start();
			}
			
			CommandResult commandResult = new SandboxExecutor()
					.directory(sandboxDir)
					.input((graderFile == null)?inputFile.getName():"pipe_in1")
					.output((graderFile == null)?outputFile.getName():"pipe_out1")
					.timeout(graderFile == null?time:3*time+1)
					.ioTimeout(getIoTimeout())
					.trusted(this instanceof JavaTestStep)
					.memory(memory)
					.extraMemory((this instanceof JavaTestStep)?0:5)
					.command(getCommand()).execute().getResult();
			result = getResult(commandResult);
			
			if (graderRun != null) {
				solutionRun.join();
				graderRun.join();
				if (result.getVerdict() == Verdict.OK) result = solutionRun.getResult();
				if (result.getVerdict() == Verdict.OK) result = graderRun.getResult();
				
				if (result.getVerdict() == Verdict.TL) result = new StepResult(Verdict.TL);
				if (result.getVerdict() == Verdict.OK) {
					double time1 = commandResult.getTime()!=null?commandResult.getTime():0;
					double time2 = solutionRun.getResult().getTime()!=null?solutionRun.getResult().getTime():0;
					double time3 = graderRun.getResult().getTime()!=null?graderRun.getResult().getTime():0;
					double totalTime = time1+time2+time3;
					if (totalTime > time) result = new StepResult(Verdict.TL);
					else result.setTime(totalTime);
				} else {
					result.setTime(null);
				}
				result.setMemory(null);
			}
			
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
	
	protected void createSandboxDirectory() {
		sandboxDir.mkdirs();
	}
	
	protected void copySandboxInput() throws Exception {
		FileUtils.copyFile(binaryFile, new File(sandboxDir, binaryFile.getName()));
		new File(sandboxDir, binaryFile.getName()).setExecutable(true);
		new ProcessExecutor().command("chmod", "+x", new File(sandboxDir, binaryFile.getName()).getAbsolutePath()).execute();
		FileUtils.copyFile(inputFile, new File(sandboxDir, inputFile.getName()));
		
		if (graderFile != null) {
			FileUtils.copyFile(graderFile, new File(sandboxDir, graderFile.getName()));
			new File(sandboxDir, graderFile.getName()).setExecutable(true);
			new ProcessExecutor().command("chmod", "+x", new File(sandboxDir, graderFile.getName()).getAbsolutePath()).execute();
		}
	}
	
	protected void createPipes() {
		if (graderFile == null) return;
		
		for (int i = 1; i <= 2; i++) {
			File pipeIn = new File(sandboxDir, "pipe_in"+i);
			File pipeOut = new File(sandboxDir, "pipe_out"+i);
			try {
				new ProcessExecutor("mkfifo", pipeIn.getAbsolutePath()).execute();
				new ProcessExecutor("mkfifo", pipeOut.getAbsolutePath()).execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void copySandboxOutput() throws IOException {
		FileUtils.copyFile(new File(sandboxDir, outputFile.getName()), outputFile);
	}
	
	public double getIoTimeout() {
		return 0;
	}
	
}
