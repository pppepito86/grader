package org.pesho.grader.task.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.pesho.grader.task.TestCase;

public class TaskTestsFinderv4 {
	
	private static final Map<String, Integer> WORD_WEIGHTS = Stream.of(
			new AbstractMap.SimpleImmutableEntry<>("in", -1),
			new AbstractMap.SimpleImmutableEntry<>("out", 1),
			new AbstractMap.SimpleImmutableEntry<>("sol", 1),
			new AbstractMap.SimpleImmutableEntry<>("ans", 1))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	
	public static void main(String[] args) throws Exception {
		Path taskPath = new File("C:\\Users\\pppep\\OneDrive\\Desktop\\tests\\task2").toPath();
		List<Path> paths = Files.walk(taskPath)
				.filter(p -> !p.toString().contains("__MACOSX"))
				.map(p -> taskPath.relativize(p))
				.collect(Collectors.toList());
		
		List<TestCase> testCases = TaskTestsFinderv4.find(paths, "peru1,perulog,perumic");
		for (TestCase testCase: testCases) {
			System.out.println(testCase.getNumber() + " " + testCase.getInput() + " " + testCase.getOutput());
		}
	}
	
	public static int getLevel(Path path)  {
		if ("".equals(path.getFileName().toString())) return 0;
		int ans = 0;
		while (path != null) {
			ans++;
			path = path.getParent();
		}
		return ans;
	}

	public static Path findTestsFolder(List<Path> paths) {
		return paths.stream()
				.filter(p -> "test".equalsIgnoreCase(p.getFileName().toString()) || "tests".equalsIgnoreCase(p.getFileName().toString()))
				.sorted(Comparator.comparingInt(TaskTestsFinderv4::getLevel).thenComparing(p -> p.toString().length()).thenComparing(p -> p))
				.findFirst().orElse(null);
	}
	
	public static List<TestCase> find(List<Path> paths, String patterns) throws IOException {
		Path testFolder = findTestsFolder(paths);
		if (testFolder == null) throw new IllegalStateException("Cannot find tests folder.");
		
		List<Path> possibleTests = paths.stream()
				.filter(p -> !p.equals(testFolder))
				.filter(p -> p.toString().startsWith(testFolder.toString()))
				.collect(Collectors.toList());
		
		List<Path> possibleInputs = possibleTests.stream()
				.sorted(Comparator.comparingInt(p -> WORD_WEIGHTS.entrySet().stream()
						.mapToInt(e -> e.getValue()*StringUtils.countMatches(p.toAbsolutePath().toString().toLowerCase(), e.getKey()))
						.sum()))
				.collect(Collectors.toList());

		Set<Path> possibleOutputs = new HashSet<>();
		while (possibleInputs.size() > possibleOutputs.size()) {
			possibleOutputs.add(possibleInputs.remove(possibleInputs.size()-1));
		}

		String[] patternsSplit = patterns.split(","); 
		possibleInputs = possibleInputs.stream().sorted((pi1, pi2) -> {
			List<String> l1 = splitName(pi1.toString());
			List<String> l2 = splitName(pi2.toString());

			for (String patternSplit: patternsSplit) {
				int c1 = StringUtils.countMatches(pi1.toString(), patternSplit);
				int c2 = StringUtils.countMatches(pi2.toString(), patternSplit);
				if (c1 != c2) return Integer.compare(c2, c1);
			}
			
			for (int i = 0; ; i++) {
				if (l1.size() == i) return -1;
				if (l2.size() == i) return 1;

				String p1 = l1.get(i);
				String p2 = l2.get(i);
				if (p1.equals(p2)) continue;
				if (!Character.isDigit(p1.charAt(0)) || !Character.isDigit(p2.charAt(0))) return p1.compareTo(p2);
				if (p1.length() == p2.length()) return p1.compareTo(p2);
				return Integer.compare(p1.length(), p2.length());
			}
		}).collect(Collectors.toList());
				
		List<TestCase> testCases = new ArrayList<>();
		for (Path input: possibleInputs) {
			Path output = possibleOutputs.stream()
					.sorted(Comparator.comparing(p -> LevenshteinDistance.computeLevenshteinDistance(input.toAbsolutePath().toString(), p.toAbsolutePath().toString())))
					.findFirst().orElse(null);
			if (output == null) continue;
			possibleOutputs.remove(output);
			testCases.add(new TestCase(testCases.size()+1, input.toAbsolutePath().toString(), output.toAbsolutePath().toString()));
		}
		
		return testCases;
	}
	
	private static List<String> splitName(String s) {
		List<String> list = new ArrayList<>();
		int start = 0;
		for (int i = 0; i < s.length(); i++) {
			if (i == s.length()-1 || Character.isDigit(s.charAt(i)) != Character.isDigit(s.charAt(i+1))) {
				if (Character.isDigit(s.charAt(i))) list.add(s.substring(start, i+1));
				start = i+1;
			}
		}
		return list;
	}

	static class LevenshteinDistance {

		private static int min(int a, int b, int c) {
			return Math.min(Math.min(a, b), c);
		}

		public static int computeLevenshteinDistance(String lhs, String rhs) {
			int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

			IntStream.range(0, lhs.length() + 1).forEach(i -> distance[i][0] = i);
			IntStream.range(0, rhs.length() + 1).forEach(j -> distance[0][j] = j);

			for (int i = 1; i <= lhs.length(); i++) {
				for (int j = 1; j <= rhs.length(); j++) {
					distance[i][j] = min(distance[i - 1][j] + 1, distance[i][j - 1] + 1,
							distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));
				}
			}

			return distance[lhs.length()][rhs.length()];
		}
	}

	
}
