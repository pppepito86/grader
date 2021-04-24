package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SolutionsFinder {

	public static List<Path> find(List<Path> paths, Set<String> extensions) {
		paths = paths.stream()
				.filter(x -> extensions.stream().anyMatch(e -> {
					return x.getFileName().toString().toLowerCase().endsWith("."+e.toLowerCase());
				}))
				.filter(x -> {
					String fileName = x.getFileName().toString();
					return !fileName.contains("checker") && !fileName.contains("system") && !fileName.contains("grader") && !fileName.contains("generator") && !fileName.contains("validator");
				})
				.filter(x -> x.getParent() == null
					|| (!"checker".equalsIgnoreCase(x.getParent().toString()) && !"system".equalsIgnoreCase(x.getParent().toString())))
				.collect(Collectors.toList());
		
		paths = paths.stream()
			.collect(Collectors.groupingBy(x -> x.getParent()!=null?x.getParent().toString():"", TreeMap::new, Collectors.toList()))
			.values().stream()
			.sorted((a, b) -> b.size() - a.size())
			.findFirst()
			.orElse(new ArrayList<>());
		
		return paths;
	}
	
}
