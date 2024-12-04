package org.pesho.grader.compile;
import java.io.File;
import java.util.Map;

public class PythonCompileStep extends CompileStep {

	public static final String COMPILE_PYPY_COMMAND_PATTERN = "/usr/bin/pypy3 -m compileall -b ./%s";
	public static final String SOURCE_FILE_ENDING = ".py";
	public static final String BINARY_FILE_ENDING = ".pyc";

	public PythonCompileStep(File sourceFile) {
		this(sourceFile, null, null, null);
	}
	
	public PythonCompileStep(File sourceFile, File graderDir, Map<String, Double> time, Map<String, Integer> memory) {
		super(sourceFile, graderDir, time, memory);
	}
	
	
	@Override
	public String[] getCommands() {
		return getCommands(COMPILE_PYPY_COMMAND_PATTERN);
	}
	
	public String[] getCommands(String pattern) {
		String command = String.format(pattern, sourceFile.getName());
		return new String[] { command };
	}
	
	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", BINARY_FILE_ENDING);
	}

}
