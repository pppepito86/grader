package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuizFinder {

	public static Optional<Path> find(List<Path> paths) {
		List<String> candidates = Arrays.asList("quiz.json", "task.json");
		
		paths = paths.stream()
				.filter(x -> candidates.contains(x.getFileName().toString().toLowerCase()))
				.collect(Collectors.toList());
		if (paths.size() == 1) return paths.stream().findFirst();

		return Optional.empty();
	}
	
}
