package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CheckerFinder {

	public static Optional<Path> find(List<Path> paths) {
		Optional<Path> maybeCppChecker = findCppChecker(paths);
		if (maybeCppChecker.isPresent()) return maybeCppChecker;
		
		List<Path> filtered = paths.stream()
				.filter(x -> x.toString().contains("checker"))
				.filter(x -> x.getFileName().toString().endsWith(".jar")
						|| x.getFileName().toString().endsWith(".sh") 
						|| !x.getFileName().toString().contains("."))
				.collect(Collectors.toList());
		if (filtered.size() == 1) return filtered.stream().findFirst();

		
		List<String> candidates = Arrays.asList("checker", "checker.sh", "checker.jar");
		for (Path path: paths) {
			String parent = Optional.ofNullable(path.getParent()).map(Path::getFileName).map(Path::toString).orElse("");
			if (candidates.contains(path.getFileName().toString().toLowerCase()) && parent.equals("checker")) {
				return Optional.of(path);
			}
		}
		for (Path path: paths) {
			if (candidates.contains(path.getFileName().toString().toLowerCase())) {
				return Optional.of(path);
			}
		}
		return Optional.empty();
	}
	
	public static Optional<Path> findCppChecker(List<Path> paths) {
		paths = paths.stream()
				.filter(x -> x.toString().contains("checker"))
				.filter(x -> x.getFileName().toString().endsWith(".cpp"))
				.collect(Collectors.toList());
		if (paths.size() == 1) return paths.stream().findFirst();
		
		for (Path path: paths) {
			String parent = Optional.ofNullable(path.getParent()).map(Path::getFileName).map(Path::toString).orElse("");
			if (path.getFileName().toString().equalsIgnoreCase("checker.cpp") && parent.equals("checker")) {
				return Optional.of(path);
			}
		}

		for (Path path: paths) {
			if (path.getFileName().toString().equals("checker.cpp")) {
				return Optional.of(path);
			}
		}

		return Optional.empty();
	}

	
}
