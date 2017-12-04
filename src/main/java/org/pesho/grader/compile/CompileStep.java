package org.pesho.grader.compile;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.Verdict;
import org.pesho.sandbox.CommandStatus;
import org.pesho.sandbox.SandboxExecutor;
import org.pesho.sandbox.SandboxResult;

public abstract class CompileStep implements BaseStep {
	
	protected final File sourceFile;
	Verdict verdict;
	
	public CompileStep(File sourceFile) {
		this.sourceFile = sourceFile.getAbsoluteFile();
	}
	
	public double execute() {
		//System.out.println("***********");
		//System.out.println("*Compiling* " + sourceFile.getAbsolutePath());
		//System.out.println(sourceFile.getParentFile());
		File sandboxDir = new File(sourceFile.getParentFile(), "sandbox_compile");
		sandboxDir.mkdirs();
		try {
			//System.out.println(" - sandbox " + sandboxDir.getAbsolutePath());
			//System.out.println(" - copying " + sourceFile.getAbsolutePath());
			FileUtils.copyFile(sourceFile, new File(sandboxDir, sourceFile.getName()));
		} catch (Exception e) {
			e.printStackTrace();
			verdict = Verdict.SE;
			return 0;
		}
				
		String[] commands = getCommands();
		for (String command: commands) {
			SandboxResult result = new SandboxExecutor()
					.directory(sandboxDir)
					.command(command)
					.execute();
			//System.out.println("Executing command: " + command);
			//System.out.println("Execution finished with result: " + result.getResult());
			//System.out.println("Compilation status: " + result.getStatus());
			if (result.getStatus() != CommandStatus.SUCCESS) {
				if (result.getStatus() != CommandStatus.SYSTEM_ERROR) {
					verdict = Verdict.CE;
				} else {
					verdict = Verdict.SE;
				}
				return 0;
			}
		}
		
		try {
			//System.out.println(new File(sandboxDir, getBinaryFileName()).getAbsolutePath());
			FileUtils.copyFile(new File(sandboxDir, getBinaryFileName()), getBinaryFile());
			//System.out.println(" - copying " + getBinaryFile().getAbsolutePath());
		} catch (Exception e) {
			//System.out.println(" FAILED");
			e.printStackTrace();
			verdict = Verdict.SE;
			return 0;
		}
		verdict = Verdict.OK;
		return 1;
	}
	
	@Override
	public Verdict getVerdict() {
		return verdict;
	}
	
	public File getBinaryFile() {
		return new File(sourceFile.getParentFile(), getBinaryFileName());
	}
	
	public abstract String getBinaryFileName();
	
	protected abstract String[] getCommands();
	
}
