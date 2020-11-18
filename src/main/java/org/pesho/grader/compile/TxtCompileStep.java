package org.pesho.grader.compile;
import java.io.File;

public class TxtCompileStep extends CompileStep {

	public static final String SOURCE_FILE_ENDING = ".txt";

	public TxtCompileStep(File sourceFile) {
		this(sourceFile, null);
	}

	public TxtCompileStep(File sourceFile, File graderDir) {
		super(sourceFile, graderDir);
	}
	
	@Override
	public void execute() {
	}

	@Override
	public String getBinaryFileName() {
		return sourceFile.getName();
	}

	@Override
	protected String[] getCommands() {
		return null;
	}

}
