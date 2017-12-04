package org.pesho.grader.compile;
import java.io.File;

public class CppCompileStep extends CompileStep {

	public static final String COMPILE_COMMAND_PATTERN = "g++ -O2 -std=c++11 -o %s %s";
	public static final String SOURCE_FILE_ENDING = ".cpp";

	public CppCompileStep(File sourceFile) {
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
