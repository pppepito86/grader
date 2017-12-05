package org.pesho.grader.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TaskParser {

	private final File taskDir;
	private List<File> input = new ArrayList<>();
	private List<File> output = new ArrayList<>();
	private List<File> solutions = new ArrayList<>();
	private File checker = new File("checker");
	private String prefix;

	public TaskParser(File dir) {
		prefix = new File(dir, ".").getAbsolutePath();
		if (prefix.endsWith(".")) {
			prefix = prefix.substring(0, prefix.length() - 1);
		}
		// System.out.println(prefix);
		taskDir = dir.getAbsoluteFile();
		parseTestsDir();
	}

	public List<File> getInput() {
		return input;
	}

	public List<File> getOutput() {
		return output;
	}

	public File getChecker() {
		return checker;
	}
	
	public List<File> getSolutions() {
		return solutions;
	}

	public int testsCount() {
		return input.size();
	}

	protected void parseTestsDir() {
		solutions = listAllFiles().stream()
				.filter(x -> x.getName().endsWith(".cpp") || x.getName().endsWith(".java"))
				.collect(Collectors.toList());
		
		findChecker();
//		System.out.println("checker:" + checker.getAbsolutePath());
		
		List<String> files = listAllFiles().stream().filter(x -> x.isFile()).map(File::getAbsolutePath)
				.filter(x -> !x.endsWith(".cpp") && !x.endsWith(".java") && !x.endsWith(".jar"))
				.collect(Collectors.toList());

		int numberOfTests = countTests(files);
		String best = findBestIn(files, numberOfTests);
		// System.out.println("best " + best);

		for (int i = 1; i <= numberOfTests; i++) {
			findTestCases(i, files, best);
		}
	}

	private int countTests(List<String> files) {
		List<String> fileNames = files.stream().map(x -> x.substring(prefix.length())).collect(Collectors.toList());

		int count = 0;
		while (true) {
			count++;
			int testCase = count;

			int max = fileNames.stream().map(x -> countNumberInString(testCase, x)).max(Integer::compare).get();
			if (max == 0)
				return count - 1;
			List<String> candidates = fileNames.stream().filter(x -> countNumberInString(testCase, x) == max)
					.collect(Collectors.toList());
			int maxInCount = candidates.stream().map(x -> substringCount(x, "in")).max(Integer::compare).get();
			if (candidates.size() < 2 || maxInCount == 0)
				return count - 1;

		}
	}
	

	private List<String> findTestCaseCandidates(int testCase, List<String> allCandidates) {
		int testCaseNumberMaxOccurrenceCount = allCandidates.stream().map(x -> countNumberInString(testCase, x)).max(Integer::compare).get();
		if (testCaseNumberMaxOccurrenceCount == 0) return new ArrayList<String>();
		List<String> testCaseCandidates = allCandidates.stream()
				.filter(x -> countNumberInString(testCase, x) == testCaseNumberMaxOccurrenceCount)
				.collect(Collectors.toList());
		return testCaseCandidates;
	}
	
//	private List<String> calculateInCandidates(int testCase, List<String> allCandidates) {
//		
//	}

	private String findBestIn(List<String> files, int maxCount) {
		List<String> fileNames = files.stream().map(x -> x.replace(prefix, "")).collect(Collectors.toList());

		for (int i = 1; i <= maxCount; i++) {
			int testCase = i;
			int max = fileNames.stream().map(x -> countNumberInString(testCase, x)).max(Integer::compare).get();
			List<String> candidates = fileNames.stream().filter(x -> countNumberInString(testCase, x) == max)
					.collect(Collectors.toList());
			int maxInCount = candidates.stream().map(x -> substringCount(x, "in")).max(Integer::compare).get();
			List<String> inCandidates = candidates.stream().filter(x -> substringCount(x, "in") == maxInCount)
					.collect(Collectors.toList());
			if (inCandidates.size() == 1)
				return inCandidates.get(0);
		}
		return null;
	}

	private List<File> listAllFiles() {
//		System.out.println("**************");
//		System.out.println(taskDir.getAbsolutePath());
		List<File> files = new ArrayList<>();
		listAllFiles(taskDir, files);
//		for (File file: files) System.out.println(file.getAbsolutePath());
		return files;
	}

	private void listAllFiles(File dir, List<File> ans) {
		if (dir.getName().startsWith("sandbox_") && (dir.getName().endsWith(".cpp") || dir.getName().endsWith(".java"))) return;
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				listAllFiles(file, ans);
			else
				ans.add(file);
		}
	}

	private void findChecker() {
		List<File> filtered = listAllFiles().stream().filter(x -> x.getAbsolutePath().contains("checker"))
				.filter(x -> x.getName().endsWith(".jar") || x.getName().endsWith(".sh") || !x.getName().contains("."))
				.collect(Collectors.toList());
		if (filtered.size() == 1) {
			this.checker = filtered.get(0);
			return;
		}
		for (File file : filtered) {
			if (file.getName().equals("checker")) {
				this.checker = file; return;
			} else if (file.getName().equals("checker.sh")) {
				this.checker = file; return;
			} else if (file.getName().equals("checker.jar")) {
				this.checker = file; return;
			}
		}
	}

	public void findTestCases(int testCaseNumber, List<String> fileNames, String best) {
		// System.out.println(testCaseNumber);
		int max = fileNames.stream().map(x -> countNumberInString(testCaseNumber, x)).max(Integer::compare).get();
		List<String> candidates = fileNames.stream().filter(x -> countNumberInString(testCaseNumber, x) == max)
				.collect(Collectors.toList());

		// System.out.println(candidates);

		int maxInCount = candidates.stream().map(x -> substringCount(x, "in")).max(Integer::compare).get();
		List<String> inCandidates = candidates.stream().filter(x -> substringCount(x, "in") == maxInCount)
				.collect(Collectors.toList());
		int minInDifference = inCandidates.stream().map(x -> LevenshteinDistance.computeLevenshteinDistance(x, best))
				.min(Integer::compare).get();
		List<String> lastInCandidates = inCandidates.stream()
				.filter(x -> LevenshteinDistance.computeLevenshteinDistance(x, best) == minInDifference)
				.collect(Collectors.toList());
		if (lastInCandidates.size() > 1) {
			throw new IllegalStateException("Cannot select input file from " + inCandidates);
		}

		String inputFile = lastInCandidates.get(0);
		candidates.remove(inputFile);

		int minDifference = candidates.stream().map(x -> LevenshteinDistance.computeLevenshteinDistance(x, inputFile))
				.min(Integer::compare).get();
		List<String> outCandidates = candidates.stream()
				.filter(x -> LevenshteinDistance.computeLevenshteinDistance(x, inputFile) == minDifference)
				.collect(Collectors.toList());

		if (outCandidates.size() > 1) {
			throw new IllegalStateException("Cannot select output file from " + inCandidates);
		}

		String outputFile = outCandidates.get(0);
		input.add(new File(inputFile));
		output.add(new File(outputFile));
	}

	public int countNumberInString(int number, String fileName) {
		String s = fileName + ".";
		int count = 0;
		int current = -1;
		for (char c : s.toCharArray()) {
			if (Character.isDigit(c)) {
				if (current >= 0) {
					current *= 10;
				} else {
					current = 0;
				}
				current += c - '0';
			} else {
				if (current == number) {
					count++;
				}
				current = -1;
			}
		}
		return count;
	}

	public int substringCount(String string, String substr) {
		return (string.length() - string.replaceAll(substr, "").length()) / substr.length();
	}

	static class LevenshteinDistance {
		private static int minimum(int a, int b, int c) {
			return Math.min(Math.min(a, b), c);
		}

		public static int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {
			int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

			for (int i = 0; i <= lhs.length(); i++)
				distance[i][0] = i;
			for (int j = 1; j <= rhs.length(); j++)
				distance[0][j] = j;

			for (int i = 1; i <= lhs.length(); i++)
				for (int j = 1; j <= rhs.length(); j++)
					distance[i][j] = minimum(distance[i - 1][j] + 1, distance[i][j - 1] + 1,
							distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

			return distance[lhs.length()][rhs.length()];
		}
	}

}
