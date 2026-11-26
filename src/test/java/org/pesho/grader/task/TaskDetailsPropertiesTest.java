package org.pesho.grader.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TaskDetailsPropertiesTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private Map<String, String> propertyErrors (String properties) throws Exception {
		Path task = folder.newFolder().toPath();
		Files.createDirectories(task.resolve("tests"));
		Files.write(task.resolve("grade.properties"), properties.getBytes(StandardCharsets.UTF_8));
		for (int i = 1; i <= 2; i++) {
			Files.write(task.resolve(String.format("tests/test.%02d.in", i)), "1\n".getBytes(StandardCharsets.UTF_8));
			Files.write(task.resolve(String.format("tests/test.%02d.sol", i)), "1\n".getBytes(StandardCharsets.UTF_8));
		}
		Map<String, String> errors = new TreeMap<>(new TaskDetails("task", task).getError());
		errors.keySet().removeIf(key -> !key.endsWith("_property"));
		return errors;
	}

	@Test
	public void invalidPropertiesHaveMessages() throws Exception {
		Map<String, String> errors = propertyErrors(String.join("\n",
			"groups=1,2",
			"dependencies=,-1",
			"weights=-1,2",
			"precision=-2",
			"io_time=abc",
			"scoring=foo",
			"scoring_type=foo",
			"extensions=cpp,rb",
			"latex=tex",
			"user_tests=yes", ""));
		assertEquals("backend.invalid_sequence", errors.get("dependencies_property"));
		assertEquals("backend.invalid_weights", errors.get("weights_property"));
		assertEquals("backend.invalid_precision", errors.get("precision_property"));
		assertEquals("backend.not_zero_or_decimal", errors.get("io_time_property"));
		assertEquals("backend.allowed_values:sum, min, min_fast", errors.get("scoring_property"));
		assertEquals("backend.allowed_values:best, aggregated", errors.get("scoring_type_property"));
		assertEquals("backend.allowed_values:cpp, c, h, java, py, go, cs, zip, txt, pdf", errors.get("extensions_property"));
		assertEquals("backend.allowed_values:lualatex, none, pdflatex, xelatex", errors.get("latex_property"));
		assertEquals("backend.allowed_values:no, no_checker, unrestricted", errors.get("user_tests_property"));
		assertTrue(errors.values().stream().noneMatch(String::isEmpty));
	}

	@Test
	public void validPropertiesHaveNoErrors() throws Exception {
		assertEquals(new TreeMap<String, String>(), propertyErrors(String.join("\n",
			"groups=1,2",
			"dependencies=,1",
			"weights=40,60",
			"precision=2",
			"io_time=0",
			"scoring=min",
			"scoring_type=best",
			"extensions=cpp,py",
			"latex=pdflatex",
			"user_tests=no_checker", "")));
	}
}
