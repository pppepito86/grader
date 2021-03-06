package org.pesho.grader.compile;
import java.io.File;

public class CSharpCompileStep extends CompileStep {

	public static final String COMPILE_COMMAND_PATTERN = "csc %s";
	public static final String SOURCE_FILE_ENDING = ".cs";
	public static final String BINARY_FILE_ENDING = ".exe";

	public CSharpCompileStep(File sourceFile) {
		this(sourceFile, null);
	}

	public CSharpCompileStep(File sourceFile, File graderDir) {
		super(sourceFile, graderDir);
	}
	
	@Override
	public String[] getCommands() {
		String command = String.format(COMPILE_COMMAND_PATTERN, getAllFiles());
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
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", BINARY_FILE_ENDING);
	}

}
