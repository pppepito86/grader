package org.pesho.grader.compile;
import java.io.File;

public class JavaNativeImageCompileStep extends CompileStep {

	public static final String COMPILE_COMMAND_PATTERN = "/usr/bin/javac -d . %s";
	public static final String JAR_COMMAND_PATTERN = "/usr/bin/jar cvfe %s %s .";
	public static final String NATIVE_IMAGE_COMMAND_PATTERN = "/usr/lib/jvm/graalvm/bin/native-image -jar %s";
	public static final String SOURCE_FILE_ENDING = ".java";
	public static final String JAR_FILE_ENDING = ".jar";
	public static final String BINARY_FILE_ENDING = ".jar";

	public JavaNativeImageCompileStep(File sourceFile, File graderDir) {
		super(sourceFile, graderDir);
	}

	@Override
	public String[] getCommands() {
		String compileCommand = String.format(COMPILE_COMMAND_PATTERN, sourceFile.getName());
		String mainClass = sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", "");
		String jarCommand = String.format(JAR_COMMAND_PATTERN, getJarFileName(), mainClass);
		String nativeImageCommand = String.format(NATIVE_IMAGE_COMMAND_PATTERN, getJarFileName());
		return new String[] { compileCommand, jarCommand, nativeImageCommand };
	}
	
	public String getJarFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", JAR_FILE_ENDING);
	}
	
	@Override
	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", "");
	}

}
