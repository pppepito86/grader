package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatementFinder {

	public static Optional<Path> find(List<Path> paths) {
		paths = paths.stream()
				.filter(x -> x.getFileName().toString().toLowerCase().endsWith("pdf"))
				.collect(Collectors.toList());
		
		if (paths.size() == 0) return Optional.empty();
		
		for (String s: new String[]{"task.pdf", "description.pdf", "statement.pdf"}) {
			if (paths.stream().filter(f -> f.getFileName().toString().toLowerCase().equals(s)).count() > 0) {
				paths = paths.stream().filter(f -> f.getFileName().toString().toLowerCase().equals(s)).collect(Collectors.toList());
				break;
			}
		}
		
		paths.sort((a, b) -> a.toString().length() - b.toString().length());
		return paths.stream().findFirst();
	}
	
}
