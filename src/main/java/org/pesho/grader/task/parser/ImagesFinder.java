package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImagesFinder {

	public static Optional<Path> find(List<Path> paths) {
		paths = paths.stream()
				.filter(x -> x.getFileName().toString().equalsIgnoreCase("images"))
				.collect(Collectors.toList());

		if (paths.size() != 1) return Optional.empty();

		return paths.stream().findFirst();
	}
	
}
