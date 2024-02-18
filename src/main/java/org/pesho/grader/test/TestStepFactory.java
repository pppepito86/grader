package org.pesho.grader.test;
import java.io.File;

import org.pesho.grader.compile.CSharpCompileStep;
import org.pesho.grader.compile.JavaCompileStep;

public class TestStepFactory {
	
	public static TestStep getInstance(File binaryFile, File managerFile, File piperFile, File inputFile, File outputFile, double time, int memory) {
		return getInstance(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, 1, 0);
	}
	
	public static TestStep getInstance(File binaryFile, File managerFile, File piperFile, File inputFile, File outputFile, double time, int memory, int processes, double ioTime) {
		if (binaryFile.getName().endsWith(ZipTestStep.BINARY_FILE_ENDING)) {
			return new ZipTestStep(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, processes);
		}
		if (binaryFile.getName().endsWith(PdfTestStep.BINARY_FILE_ENDING)) {
			return new PdfTestStep(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, processes);
		}
		if (binaryFile.getName().endsWith(TxtTestStep.BINARY_FILE_ENDING)) {
			return new TxtTestStep(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, processes);
		}
		if (binaryFile.getName().endsWith(JavaCompileStep.BINARY_FILE_ENDING)) {
			return new JavaTestStep(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, processes);
		}
		if (binaryFile.getName().endsWith(CSharpCompileStep.BINARY_FILE_ENDING)) {
			return new CSharpTestStep(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, processes);
		}
		return new CppTestStep(binaryFile, managerFile, piperFile, inputFile, outputFile, time, memory, processes, ioTime);
	}

}
