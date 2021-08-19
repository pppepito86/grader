package org.pesho.grader.test;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.exec.ProcessExecutor;

public class CppPipeTestStep extends CppTestStep {

	private File graderFile;

	public CppPipeTestStep(File binaryFile, File graderFile, File inputFile, File outputFile, double time, int memory, double ioTime) {
		super(binaryFile, inputFile, outputFile, time, memory, ioTime);
		this.graderFile = graderFile;
	}
	
	private void createPipes(File pipeIn, File pipeOut) {
		try {
			new ProcessExecutor("mkfifo", pipeIn.getAbsolutePath()).execute();
			new ProcessExecutor("mkfifo", pipeOut.getAbsolutePath()).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getCommand() {
		return String.format(CppTestStep.EXECUTE_COMMAND_PATTERN, graderFile.getName());
	}
	
	protected void copySandboxInput() throws Exception {
		createPipes(new File(sandboxDir, "pipe_in"), new File(sandboxDir, "pipe_out"));

		FileUtils.copyFile(binaryFile, new File(sandboxDir, binaryFile.getName()));
		new File(sandboxDir, binaryFile.getName()).setExecutable(true);
		new ProcessExecutor().command("chmod", "+x", new File(sandboxDir, binaryFile.getName()).getAbsolutePath()).execute();

		FileUtils.copyFile(graderFile, new File(sandboxDir, graderFile.getName()));
		new File(sandboxDir, graderFile.getName()).setExecutable(true);
		new ProcessExecutor().command("chmod", "+x", new File(sandboxDir, graderFile.getName()).getAbsolutePath()).execute();

		FileUtils.copyFile(inputFile, new File(sandboxDir, inputFile.getName()));
	}

}
