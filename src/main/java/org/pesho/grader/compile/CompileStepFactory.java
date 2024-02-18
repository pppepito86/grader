package org.pesho.grader.compile;
import java.io.File;
import java.util.Map;

public class CompileStepFactory {
	
	public static CompileStep getInstance(File sourceFile, File graderDir, Map<String, Double> time, Map<String, Integer> memory) {
		if (sourceFile.getName().endsWith(CCompileStep.SOURCE_FILE_ENDING)) {
			return new CCompileStep(sourceFile, graderDir, time, memory);
		}
		if (sourceFile.getName().endsWith(HCompileStep.SOURCE_FILE_ENDING)) {
			return new HCompileStep(sourceFile, graderDir, time, memory);
		}
		if (sourceFile.getName().endsWith(CppCompileStep.SOURCE_FILE_ENDING)) {
			return new CppCompileStep(sourceFile, graderDir, time, memory);
		}
		if (sourceFile.getName().endsWith(JavaCompileStep.SOURCE_FILE_ENDING)) {
			return new JavaCompileStep(sourceFile, graderDir, time, memory);
		}
		if (sourceFile.getName().endsWith(CSharpCompileStep.SOURCE_FILE_ENDING)) {
			return new CSharpCompileStep(sourceFile, graderDir, time, memory);
		}
		if (sourceFile.getName().endsWith(GoCompileStep.SOURCE_FILE_ENDING)) {
			return new GoCompileStep(sourceFile, graderDir, time, memory);
		}
		if (sourceFile.getName().endsWith(ZipCompileStep.SOURCE_FILE_ENDING)) {
			return new ZipCompileStep(sourceFile, graderDir, time, memory);
		}
		
		return new NoCompileStep(sourceFile, graderDir, time, memory);
	}

}
