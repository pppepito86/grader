package org.pesho.grader.compile;
import java.io.File;
import java.util.Map;

public class CCompileStep extends CompileStep {

	public static final String COMPILE_COMMAND_PATTERN = "gcc -std=c99 -o %s %s";
	public static final String SOURCE_FILE_ENDING = ".c";

	public CCompileStep(File sourceFile) {
		this(sourceFile, null, null, null);
	}
	
	public CCompileStep(File sourceFile, File graderDir, Map<String, Double> time, Map<String, Integer> memory) {
		super(sourceFile, graderDir, time, memory);
	}
	
	@Override
	public String[] getCommands() {
		String compiledFileName = getBinaryFileName();
		String command = String.format(COMPILE_COMMAND_PATTERN, compiledFileName, getAllFiles());
		return new String[] { command };
	}
	
	private String getAllFiles() {
		String files = sourceFile.getName();
		if (graderDir != null && graderDir.exists()) {
			for (File file: graderDir.listFiles()) files += " " + file.getName();
		}
		return files;
	}
	
	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", "");
	}

}
