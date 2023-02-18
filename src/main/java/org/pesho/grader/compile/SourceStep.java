package org.pesho.grader.compile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.BaseStep;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;

public class SourceStep implements BaseStep {

	private File sourceFile;
	private Set<String> words;
	private StepResult result;
	
	public SourceStep(File sourceFile, Set<String> words) {
		this.sourceFile = sourceFile;
		this.words = words;
	}
	
	@Override
	public void execute() {
		String message = "";
		try {
			String source = FileUtils.readFileToString(sourceFile, StandardCharsets.UTF_8);
			for (String word: words) {
				if (source.contains(word)) message += word + ", ";
			}
			if (message.isEmpty()) {
				result = new StepResult(Verdict.OK);
			} else {
				message = message.trim();
				message = message.substring(0, message.length()-1);
				result = new StepResult(Verdict.CE, message);
			}
		} catch (Exception e) {
			result = new StepResult(Verdict.CE, "Source file check failed");
		}
	}
	
	@Override
	public StepResult getResult() {
		return result;
	}
	
	public static boolean containsWord(File file, String word) throws Exception {
		return FileUtils.readFileToString(file, StandardCharsets.UTF_8).contains(word);
	}

	@Override
	public Verdict getVerdict() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
