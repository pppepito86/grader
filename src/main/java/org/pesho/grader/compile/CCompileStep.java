package org.pesho.grader.compile;
import java.io.File;

public class CCompileStep extends CompileStep {

	public static final String COMPILE_COMMAND_PATTERN = "gcc -c -std=c99 -o %s %s";
	public static final String SOURCE_FILE_ENDING = ".c";

	public CCompileStep(File sourceFile) {
		super(sourceFile);
	}
	
	@Override
	public String[] getCommands() {
		String compiledFileName = getBinaryFileName();
		String command = String.format(COMPILE_COMMAND_PATTERN, compiledFileName, sourceFile.getName());
		return new String[] { command };
	}
	
	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", "");
	}

}
