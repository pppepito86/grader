package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GraderFinder {

	public static Optional<Path> find(List<Path> paths) {
		paths = paths.stream()
				.filter(x -> x.getFileName().toString().equalsIgnoreCase("grader.cpp"))
				.collect(Collectors.toList());
		if (paths.size() == 1) return paths.stream().findFirst();

		return Optional.empty();
	}
	
}