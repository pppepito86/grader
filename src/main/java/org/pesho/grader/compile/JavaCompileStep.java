package org.pesho.grader.compile;
import java.io.File;
import java.util.Map;

public class JavaCompileStep extends CompileStep {

	public static final String COMPILE_COMMAND_PATTERN = "/usr/bin/javac -d . %s";
	public static final String JAR_COMMAND_PATTERN = "/usr/bin/jar cvfe %s %s .";
	public static final String SOURCE_FILE_ENDING = ".java";
	public static final String BINARY_FILE_ENDING = ".jar";

	public JavaCompileStep(File sourceFile, File graderDir, Map<String, Double> time, Map<String, Integer> memory) {
		super(sourceFile, graderDir, time, memory);
	}

	@Override
	public String[] getCommands() {
		String compileCommand = String.format(COMPILE_COMMAND_PATTERN, getAllFiles());
		String jarCommand = String.format(JAR_COMMAND_PATTERN, getBinaryFileName(), getMainClassName());
		return new String[] { compileCommand, jarCommand };
	}
	
	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", BINARY_FILE_ENDING);
	}

	public String getMainClassName() {
		return graderDir == null ? sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", ""):"grader";
//		try {
//			String mainClass = sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", ".class");
//			if (graderDir != null) {
//				mainClass = new File(sourceFile.getParentFile(), "grader.java").getName().replaceAll(SOURCE_FILE_ENDING + "$", ".class");
//			}
//			String fullPath = new ProcessExecutor()
//					.command(Arrays.asList("find", ".", "-name", mainClass))
//					.directory(sandboxDir)
//					.readOutput(true)
//					.execute()
//					.outputString()
//					.trim();
//			return fullPath.replaceAll(".class$", "").replace("/", ".").substring(2);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new File(sourceFile.getParentFile(), "grader.java").getName().replaceAll(SOURCE_FILE_ENDING + "$", ".class");
//		}
	}
	
	private String getAllFiles() {
		String files = sourceFile.getName();
		if (graderDir != null && graderDir.exists()) {
			for (File file: graderDir.listFiles()) {
				if (!file.isFile()) continue;
				if (file.getName().equals(sourceFile.getName())) continue;
				
				files += " " + file.getName();
			}
		}
		return files;
	}
	
}
