package org.pesho.grader.compile;
import java.io.File;
import java.util.Map;

import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;

public class NoCompileStep extends CompileStep {

	public NoCompileStep(File sourceFile) {
		this(sourceFile, null, null, null);
	}
	
	public NoCompileStep(File sourceFile, File graderDir, Map<String, Double> time, Map<String, Integer> memory) {
		super(sourceFile, graderDir, time, memory);
	}
	
	@Override
	public void execute() {
		this.result = new StepResult(Verdict.OK);
	}

	@Override
	public String getBinaryFileName() {
		return sourceFile.getName();
	}

	@Override
	protected String[] getCommands() {
		return null;
	}

}
