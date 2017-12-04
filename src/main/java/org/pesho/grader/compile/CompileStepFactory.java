package org.pesho.grader.compile;
import java.io.File;

public class CompileStepFactory {
	
	public static CompileStep getInstance(File sourceFile) {
		if (sourceFile.getName().endsWith(CppCompileStep.SOURCE_FILE_ENDING)) {
			return new CppCompileStep(sourceFile);
		}
		if (sourceFile.getName().endsWith(JavaCompileStep.SOURCE_FILE_ENDING)) {
			return new JavaCompileStep(sourceFile);
		}
		throw new IllegalStateException("source file language is not supported");
	}

}
