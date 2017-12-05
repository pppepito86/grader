package org.pesho.grader.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
		solutions = listAllFiles().stream().filter(x -> x.getName().endsWith(".cpp") || x.getName().endsWith(".java"))
				.collect(Collectors.toList());

		findChecker();

		List<String> files = listAllFiles().stream().filter(x -> x.isFile()).map(File::getAbsolutePath)
				.filter(x -> !x.endsWith(".cpp") && !x.endsWith(".java") && !x.endsWith(".jar"))
				.collect(Collectors.toList());

		int numberOfTests = countTests(files);
		String bestInputCandidate = findBestInputCandidate(files, numberOfTests);

		for (int i = 1; i <= numberOfTests; i++) {
			findTestCases(i, files, bestInputCandidate);
		}
	}

	private int countTests(List<String> files) {
		List<String> fileNames = files.stream().map(x -> x.substring(prefix.length())).collect(Collectors.toList());

		for (int i = 1;; i++) {
			List<String> testCaseCandidates = findTestCaseCandidates(i, fileNames);
			List<String> inputTestCaseCandidates = findInputCandidates(testCaseCandidates);
			if (testCaseCandidates.size() < 2 || inputTestCaseCandidates.size() < 1) {
				return i - 1;
			}
		}
	}

	private List<String> findTestCaseCandidates(int testCase, List<String> allCandidates) {
		int testCaseNumberMaxOccurrenceCount = allCandidates.stream().map(x -> countNumberInString(testCase, x))
				.max(Integer::compare).orElse(0);
		if (testCaseNumberMaxOccurrenceCount == 0) {
			return new ArrayList<String>();
		}
		List<String> testCaseCandidates = allCandidates.stream()
				.filter(x -> countNumberInString(testCase, x) == testCaseNumberMaxOccurrenceCount)
				.collect(Collectors.toList());
		return testCaseCandidates;
	}

	private List<String> findInputCandidates(List<String> candidates) {
		int inputMaxOccurrenceCount = candidates.stream().map(x -> countInSubstrings(x)).max(Integer::compare).orElse(0);
		if (inputMaxOccurrenceCount == 0) return new ArrayList<>();
		List<String> inputCandidates = candidates.stream().filter(x -> substringCount(x, "in") == inputMaxOccurrenceCount)
				.collect(Collectors.toList());
		return inputCandidates;
	}

	private String findBestInputCandidate(List<String> files, int testsCount) {
		List<String> fileNames = files.stream().map(x -> x.replace(prefix, "")).collect(Collectors.toList());

		for (int i = 1; i <= testsCount; i++) {
			List<String> testCaseCandidates = findTestCaseCandidates(i, fileNames);
			List<String> testCaseInputCandidates = findInputCandidates(testCaseCandidates);
			if (testCaseInputCandidates.size() == 1) {
				return testCaseInputCandidates.get(0);
			}
		}
		return null;
	}

	private List<File> listAllFiles() {
		List<File> allFiles = new ArrayList<>();
		listAllFiles(taskDir, allFiles);
		return allFiles;
	}

	private void listAllFiles(File dir, List<File> allFiles) {
		if (dir.getName().startsWith("sandbox_"))
			return;
		Arrays.stream(dir.listFiles()).filter(File::isFile).forEach(allFiles::add);
		Arrays.stream(dir.listFiles()).filter(File::isDirectory).forEach(x -> listAllFiles(x, allFiles));
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
				this.checker = file;
				return;
			} else if (file.getName().equals("checker.sh")) {
				this.checker = file;
				return;
			} else if (file.getName().equals("checker.jar")) {
				this.checker = file;
				return;
			}
		}
	}

	public void findTestCases(int testCaseNumber, List<String> fileNames, String best) {
		List<String> testCaseCandidates = findTestCaseCandidates(testCaseNumber, fileNames);
		List<String> allInputCandidates = findInputCandidates(testCaseCandidates);
		List<String> closestInputCandidates = allInputCandidates;
		if (best != null) {
			closestInputCandidates = findClosestCandidates(best, allInputCandidates);
		}
		if (closestInputCandidates.size() > 1) {
			throw new IllegalStateException("Cannot select input file from " + closestInputCandidates);
		}

		String inputFile = closestInputCandidates.get(0);
		testCaseCandidates.remove(inputFile);

		List<String> closestOutputCandidates = findClosestCandidates(inputFile, testCaseCandidates);

		if (closestOutputCandidates.size() > 1) {
			throw new IllegalStateException("Cannot select output file from " + closestOutputCandidates);
		}

		String outputFile = closestOutputCandidates.get(0);
		input.add(new File(inputFile));
		output.add(new File(outputFile));
	}

	private List<String> findClosestCandidates(String targetValue, List<String> candidates) {
		int minDifference = candidates.stream().map(x -> LevenshteinDistance.computeLevenshteinDistance(x, targetValue))
				.min(Integer::compare).get();
		List<String> closestCandidates = candidates.stream()
				.filter(x -> LevenshteinDistance.computeLevenshteinDistance(x, targetValue) == minDifference)
				.collect(Collectors.toList());
		return closestCandidates;
	}

	public int countNumberInString(int number, String fileName) {
		return (int) Arrays.stream(fileName.replaceAll("[^0-9]", " ").split(" "))
				.filter(x -> x.length() > 0)
				.map(Integer::valueOf)
				.filter(x -> x == number).count();
	}

	public int countInSubstrings(String string) {
		return substringCount(string.toLowerCase(), "in");
	}

	public int substringCount(String string, String substr) {
		return (string.length() - string.replaceAll(substr, "").length()) / substr.length();
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
