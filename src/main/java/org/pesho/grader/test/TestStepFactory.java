package org.pesho.grader.test;
import java.io.File;

import org.pesho.grader.compile.CSharpCompileStep;
import org.pesho.grader.compile.JavaCompileStep;

public class TestStepFactory {
	
	public static TestStep getInstance(File binaryFile, File inputFile, File outputFile, double time, int memory) {
		if (binaryFile.getName().endsWith(JavaCompileStep.BINARY_FILE_ENDING)) {
			return new JavaTestStep(binaryFile, inputFile, outputFile, time, memory);
		}
		if (binaryFile.getName().endsWith(CSharpCompileStep.BINARY_FILE_ENDING)) {
			return new CSharpTestStep(binaryFile, inputFile, outputFile, time, memory);
		}
		return new CppTestStep(binaryFile, inputFile, outputFile, time, memory);
	}

}
