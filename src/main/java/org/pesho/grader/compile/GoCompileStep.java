package org.pesho.grader.compile;
import java.io.File;
import java.util.Map;

public class GoCompileStep extends CompileStep {

	public static final String COMPILE_COMMAND_PATTERN = "/usr/bin/go build -o %s";
	public static final String SOURCE_FILE_ENDING = ".go";

	public GoCompileStep(File sourceFile) {
		this(sourceFile, null, null, null);
	}
	
	public GoCompileStep(File sourceFile, File graderDir, Map<String, Double> time, Map<String, Integer> memory) {
		super(sourceFile, graderDir, time, memory);
	}
	
	@Override
	public String[] getCommands() {
		String command = String.format(COMPILE_COMMAND_PATTERN, getBinaryFileName());
		return new String[] { command };
	}

	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", "");
	}

}
