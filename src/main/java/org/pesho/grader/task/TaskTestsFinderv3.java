package org.pesho.grader.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TaskTestsFinderv3 {
	
	public static void main(String[] args) throws Exception {
		File f = new File("C:\\Users\\Petar\\Downloads\\АК2_ctree");
		new TaskTestsFinderv3().findTests(f.toPath(), "^tests\\\\input.[0-9]+.*$", "^tests\\\\output.[0-9]+.*$");
	}
	
	public List<TestCase> findTests(Path dir, String inputString, String outputString) throws IOException {
		Pattern inputPattern = Pattern.compile(inputString);
		Pattern outputPattern = Pattern.compile(outputString);
		List<String> inputs = new ArrayList<>();
		List<String> outputs = new ArrayList<>();
		Files.walk(dir)
				.filter(p -> !p.toString().contains("__MACOSX"))
				.filter(Files::isRegularFile)
				.map(p -> dir.relativize(p))
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

