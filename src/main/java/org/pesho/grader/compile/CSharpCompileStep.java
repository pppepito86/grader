package org.pesho.grader.compile;
import java.io.File;
import java.util.Map;

public class CSharpCompileStep extends CompileStep {

	public static final String COMPILE_COMMAND_PATTERN = "csc %s";
	public static final String SOURCE_FILE_ENDING = ".cs";
	public static final String BINARY_FILE_ENDING = ".exe";

	public CSharpCompileStep(File sourceFile) {
		this(sourceFile, null, null, null);
	}
	
	public CSharpCompileStep(File sourceFile, File graderDir, Map<String, Double> time, Map<String, Integer> memory) {
		super(sourceFile, graderDir, time, memory);
	}
	
	@Override
	public String[] getCommands() {
		String command = String.format(COMPILE_COMMAND_PATTERN, getAllFiles());
		return new String[] { command };
	}
	
	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", BINARY_FILE_ENDING);
	}

}
