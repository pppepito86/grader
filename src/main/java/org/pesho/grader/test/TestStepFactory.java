package org.pesho.grader.test;
import java.io.File;

import org.pesho.grader.compile.JavaCompileStep;

public class TestStepFactory {
	
	public static TestStep getInstance(File binaryFile, File inputFile, File outputFile) {
		if (binaryFile.getName().endsWith(JavaCompileStep.BINARY_FILE_ENDING)) {
			return new JavaTestStep(binaryFile, inputFile, outputFile);
		}
		return new CppTestStep(binaryFile, inputFile, outputFile);
		//throw new IllegalStateException("binary file language is not supported");
	}

}
