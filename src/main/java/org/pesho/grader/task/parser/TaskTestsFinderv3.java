package org.pesho.grader.task.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.pesho.grader.task.TestCase;

public class TaskTestsFinderv3 {
	
	public List<TestCase> find(List<Path> paths, String inputString, String outputString) throws IOException {
		Pattern inputPattern = Pattern.compile(inputString);
		Pattern outputPattern = Pattern.compile(outputString);
		List<String> inputs = new ArrayList<>();
		List<String> outputs = new ArrayList<>();
		paths.stream()
				.map(Path::toString)
				.forEach(path -> {
					if (inputPattern.matcher(path).matches()) inputs.add(path);
					if (outputPattern.matcher(path).matches()) outputs.add(path);
				});
		
		Collections.sort(inputs);
		Collections.sort(outputs);
		
		List<TestCase> tests = new ArrayList<>();
		if (inputs.size() != outputs.size()) return tests;
		
		for (int i = 0; i < inputs.size(); i++) {
			tests.add(new TestCase(i+1, inputs.get(i), outputs.get(i)));
		}
		return tests;
	}
	
}

