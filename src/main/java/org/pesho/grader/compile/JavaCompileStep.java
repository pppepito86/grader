package org.pesho.grader.compile;
import java.io.File;
import java.util.Arrays;

import org.zeroturnaround.exec.ProcessExecutor;

public class JavaCompileStep extends CompileStep {

	public static final String COMPILE_COMMAND_PATTERN = "/usr/bin/javac -d . %s";
	public static final String JAR_COMMAND_PATTERN = "/usr/bin/jar cvfe %s %s .";
	public static final String SOURCE_FILE_ENDING = ".java";
	public static final String BINARY_FILE_ENDING = ".jar";

	public JavaCompileStep(File sourceFile, File graderDir) {
		super(sourceFile, graderDir);
	}

	@Override
	public String[] getCommands() {
		String compileCommand = String.format(COMPILE_COMMAND_PATTERN, sourceFile.getName());
		String mainClass = sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", "");
//		String jarCommand = String.format(JAR_COMMAND_PATTERN, getBinaryFileName(), mainClass);
		return new String[] { compileCommand, JAR_COMMAND_PATTERN };
	}
	
	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", BINARY_FILE_ENDING);
	}

	public String getMainClassName() {
		try {
			String mainClass = sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", ".class");
			String fullPath = new ProcessExecutor()
					.command(Arrays.asList("find", ".", "-name", mainClass))
					.directory(sandboxDir)
					.readOutput(true)
					.execute()
					.outputString()
					.trim();
			return fullPath.replaceAll(SOURCE_FILE_ENDING + "$", "").replaceAll("/", ".").replaceAll("..", ".");
		} catch (Exception e) {
			e.printStackTrace();
			return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", "");
		}
	}
	
}
