package org.pesho.grader.task.parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.pesho.grader.task.TestCase;

public class TaskTestsFinderv2 {
	
	private static final Map<String, Integer> WORD_WEIGHTS = Stream.of(
			new AbstractMap.SimpleImmutableEntry<>("test", -1),
			new AbstractMap.SimpleImmutableEntry<>("in", -1),
			new AbstractMap.SimpleImmutableEntry<>("out", 1),
			new AbstractMap.SimpleImmutableEntry<>("sol", 1),
			new AbstractMap.SimpleImmutableEntry<>("ans", 1))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	
	public static List<TestCase> find(List<Path> paths, Path basePath, boolean hasChecker) throws IllegalStateException {
		Set<String> pathsSet = paths.stream()
			.filter(p -> Files.isRegularFile(basePath.resolve(p)))
			.map(Path::toString)
			.collect(Collectors.toSet());
		
		List<PathPattern> patterns = getTestPatterns(pathsSet);
		List<Integer> testsNumbers = patterns.stream()
				.findFirst()
				.map(p -> findMatches(p, pathsSet))
				.orElse(new ArrayList<>());
		
		if (patterns.size() == 0) throw new IllegalStateException("backend.no_patterns");
		if (!hasChecker && patterns.size() != 2) {
			throw new IllegalStateException("backend.patterns: " + patterns);
		}
		
		return testsNumbers.stream()
				.map(i -> new TestCase(i, patterns.get(0).replace(i), (patterns.size()==2)?patterns.get(1).replace(i):null))
				.collect(Collectors.toList());
	}
	
	public static List<PathPattern> getTestPatterns(Set<String> pathsSet) {
		if (pathsSet.stream()
				.flatMap(p -> getPatterns(p).stream())
				.collect(Collectors.groupingBy(pattern -> findMatches(pattern, pathsSet).size(), TreeMap::new, Collectors.toList()))
				.size() == 0) return new ArrayList<>();
		
		List<PathPattern> patternCandidates = pathsSet.stream()
				.flatMap(p -> getPatterns(p).stream())
				.collect(Collectors.groupingBy(pattern -> findMatches(pattern, pathsSet).size(), TreeMap::new, Collectors.toList()))
				.lastEntry()
				.getValue();
		
//		System.out.println(patternCandidates);
		
		List<PathPattern> patterns = patternCandidates.stream()
				.sorted(Comparator.comparing(PathPattern::getPath))
				.limit(2)
				.sorted(Comparator.comparingInt(p -> WORD_WEIGHTS.entrySet().stream()
						.mapToInt(e -> e.getValue()*StringUtils.countMatches(p.getPath().toLowerCase(), e.getKey()))
						.sum()))
				.collect(Collectors.toList());
		
//		for (PathPattern pattern: patterns) {
//			int matches = countMatches(pattern, paths);
//			System.out.println(pattern.getPath() + " " + pattern.getReplacements() + " " + matches);
//		}
		return patterns;
	}

	private static List<PathPattern> getPatterns(String path) {
		PathPattern pattern = getPattern(path);
		List<PathPattern> patterns = new ArrayList<>();
		patterns.addAll(pattern.getSinglePattenrs());
		
		return patterns;
	}
	
	private static PathPattern getPattern(String path) {
		StringBuffer pattern = new StringBuffer();
		Pattern p = Pattern.compile("0*1");
		Matcher m = p.matcher(path);
		List<String> replacements = new ArrayList<>();
		while (m.find()) {
			m.appendReplacement(pattern, "{"+replacements.size()+"}");
			
			if (m.group().startsWith("0")) replacements.add("%0"+m.group().length()+"d");
			else replacements.add("%d");
		}
		m.appendTail(pattern);
		return new PathPattern(pattern.toString(), replacements);
	}
	
	private static List<Integer> findMatches(PathPattern pattern, Set<String> paths) {
		List<Integer> numbers = new ArrayList<>();
		int curr = 0;
		while (true) {
			String number = String.format(pattern.getReplacements().get(0), curr);
			String path = pattern.getPath().replace("{0}", number);
			if (!paths.contains(path) && curr != 0) return numbers;
			if (paths.contains(path)) numbers.add(curr);
			
			curr++;
		}
	}

}

class PathPattern {
	
	private String path;
	private List<String> replacements;
	
	PathPattern(String path, List<String> replacements) {
		this.path = path;
		this.replacements = replacements;
	}
	
	public String getPath() {
		return path;
	}
	
	public List<String> getReplacements() {
		return replacements;
	}
	
	public String replace(int... numbers) {
		String curr = path;
		for (int i = 0; i < numbers.length; i++) {
			curr = path.replace("{"+i+"}", String.format(replacements.get(i), numbers[i]));
		}
		return curr;
	}
	
	public List<PathPattern> getSinglePattenrs() {
		List<PathPattern> patterns = new ArrayList<>();
		for (int i = 0; i < replacements.size(); i++) {
			String curr = path;
			
			for (int j = 0; j < replacements.size(); j++) {
				if (i == j) continue;
				curr = curr.replace("{"+j+"}", String.format(replacements.get(j), 1));
			}
			curr = curr.replace("{"+i+"}", "{0}");
			patterns.add(new PathPattern(curr, Arrays.asList(replacements.get(i))));
		}
		return patterns;
	}
	
	public List<PathPattern> getDoublePattenrs() {
		List<PathPattern> patterns = new ArrayList<>();
		for (int i = 0; i < replacements.size(); i++) {
			for (int j = i+1; j < replacements.size(); j++) {
				String curr = path;
				for (int k = 0; k < replacements.size(); k++) {
					if (k == i || k == j) continue;
					curr = curr.replace("{"+k+"}", String.format(replacements.get(k), 1));
				}
				curr = curr.replace("{"+i+"}", "{0}");
				curr = curr.replace("{"+j+"}", "{1}");
				patterns.add(new PathPattern(curr, Arrays.asList(replacements.get(i), replacements.get(j))));
			}
		}
		return patterns;
	}
	
	@Override
	public String toString() {
		return path + " " +replacements;
	}
	
}
