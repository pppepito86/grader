package org.pesho.grader.compile;
import java.io.File;

public class CompileStepFactory {
	
	public static CompileStep getInstance(File sourceFile, File graderDir) {
		if (sourceFile.getName().endsWith(CCompileStep.SOURCE_FILE_ENDING)) {
			return new CCompileStep(sourceFile, graderDir);
		}
		if (sourceFile.getName().endsWith(CppCompileStep.SOURCE_FILE_ENDING)) {
			return new CppCompileStep(sourceFile, graderDir);
		}
		if (sourceFile.getName().endsWith(JavaCompileStep.SOURCE_FILE_ENDING)) {
			return new JavaCompileStep(sourceFile, graderDir);
		}
		throw new IllegalStateException("source file language is not supported");
	}

}
