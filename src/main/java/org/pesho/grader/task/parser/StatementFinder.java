package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatementFinder {

	public static Optional<Path> find(String analysis, List<Path> paths) {
		if (analysis != null) analysis=removeExtension(analysis.toLowerCase());
		final String analysisName=analysis;
		paths = paths.stream()
				.filter(x -> {
					String name=x.getFileName().toString().toLowerCase();
					if (!name.endsWith("pdf")) return false;
					String path=removeExtension(x.toString().toLowerCase());
                    if (path.equals(analysisName)) return false;
					if (path.contains("analysis") || path.contains("solution") ||
						path.contains("analiz") || path.contains("reshenie")) return false;
					return true;
				})
				.collect(Collectors.toList());
		
		if (paths.size() == 0) return Optional.empty();
		
		for (String s: new String[]{"task.pdf", "description.pdf", "statement.pdf"}) {
			if (paths.stream().filter(f -> f.getFileName().toString().toLowerCase().equals(s)).count() > 0) {
				paths = paths.stream().filter(f -> f.getFileName().toString().toLowerCase().equals(s)).collect(Collectors.toList());
				break;
			}
		}
		if (paths.stream().filter(f -> f.toString().toLowerCase().contains("statement")).count() > 0) {
			paths = paths.stream().filter(f -> f.toString().toLowerCase().contains("statement")).collect(Collectors.toList());
		}
		
		paths.sort((a, b) -> a.toString().length() - b.toString().length());
		return paths.stream().findFirst();
	}
	
	private static String removeExtension (String name) {
		String[] parts=name.split(".");
		if (parts.length<2) return name;
		String res="";
		for (int i=0; i<=parts.length-2; i++) {
			res+=parts[i];
			if (i<parts.length-2) res+=".";
		}
		return res;
	}
}
