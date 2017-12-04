package org.pesho.grader.check;
import java.io.File;

import org.pesho.grader.compile.JavaCompileStep;

public class CheckStepFactory {
	
	public static CheckStep getInstance(File binaryFile, File inputFile, File outputFile, File solutionFile) {
		if (binaryFile.getName().endsWith(JavaCompileStep.BINARY_FILE_ENDING)) {
			return new JavaCheckStep(binaryFile, inputFile, outputFile, solutionFile);
		}
		return new CppCheckStep(binaryFile, inputFile, outputFile, solutionFile);
		//throw new IllegalStateException("binary file language is not supported");
	}

}
