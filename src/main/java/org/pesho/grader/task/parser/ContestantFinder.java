package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ContestantFinder {

	public static Optional<Path> find(List<Path> paths) {
		Optional<Path> maybeZip = paths.stream()
				.filter(x -> x.getFileName().toString().equalsIgnoreCase("contestant.zip"))
				.sorted((a, b) -> a.toString().length() - b.toString().length())
				.findFirst();
		if (maybeZip.isPresent()) return maybeZip;

		return paths.stream()
				.filter(x -> x.getFileName().toString().equalsIgnoreCase("contestant"))
				.sorted((a, b) -> b.toString().length() - a.toString().length())
				.map(p -> p.resolve("contestant.zip"))
				.findFirst();
	}

}
