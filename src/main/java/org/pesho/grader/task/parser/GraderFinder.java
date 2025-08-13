package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GraderFinder {

	public static Optional<Path> find(List<Path> paths, Set<String> extensions) {
		paths = paths.stream()
				.filter(x -> x.getFileName().toString().equalsIgnoreCase("grader.cpp"))
				.collect(Collectors.toList());
		if (extensions.size() > 1) return paths.stream().findFirst().map(p -> p.getParent());

		if (paths.size() != 1) {
                        paths = paths.stream().filter(f -> f.toString().toLowerCase().contains("system")).collect(Collectors.toList());
                        if (paths.size() == 0) return Optional.empty();
                }


                paths.sort((a, b) -> a.toString().length() - b.toString().length());
		return paths.stream().findFirst();
	}
	
}
