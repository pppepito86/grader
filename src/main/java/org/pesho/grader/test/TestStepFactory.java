package org.pesho.grader.test;
import java.io.File;

import org.pesho.grader.compile.CSharpCompileStep;
import org.pesho.grader.compile.JavaCompileStep;

public class TestStepFactory {
	
	public static TestStep getInstance(File binaryFile, File graderFile, File inputFile, File outputFile, double time, int memory) {
		return getInstance(binaryFile, graderFile, inputFile, outputFile, time, memory, 0);
	}
	
	public static TestStep getInstance(File binaryFile, File graderFile, File inputFile, File outputFile, double time, int memory, double ioTime) {
		if (binaryFile.getName().endsWith(ZipTestStep.BINARY_FILE_ENDING)) {
			return new ZipTestStep(binaryFile, graderFile, inputFile, outputFile, time, memory);
		}
		if (binaryFile.getName().endsWith(PdfTestStep.BINARY_FILE_ENDING)) {
			return new PdfTestStep(binaryFile, graderFile, inputFile, outputFile, time, memory);
		}
		if (binaryFile.getName().endsWith(TxtTestStep.BINARY_FILE_ENDING)) {
			return new TxtTestStep(binaryFile, graderFile, inputFile, outputFile, time, memory);
		}
		if (binaryFile.getName().endsWith(JavaCompileStep.BINARY_FILE_ENDING)) {
			return new JavaTestStep(binaryFile, graderFile, inputFile, outputFile, time, memory);
		}
		if (binaryFile.getName().endsWith(CSharpCompileStep.BINARY_FILE_ENDING)) {
			return new CSharpTestStep(binaryFile, graderFile, inputFile, outputFile, time, memory);
		}
//		if (graderFile != null) {
//			return new CppPipeTestStep(binaryFile, graderFile, inputFile, outputFile, time, memory, ioTime);
//		}
		return new CppTestStep(binaryFile, graderFile, inputFile, outputFile, time, memory, ioTime);
	}

}
