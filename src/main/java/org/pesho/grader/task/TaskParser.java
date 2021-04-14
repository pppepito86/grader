package org.pesho.grader.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TaskParser {

	private final File taskDir;
	private List<File> input = new ArrayList<>();
	private List<File> output = new ArrayList<>();
	private List<File> solutions = new ArrayList<>();
	private File checker = new File("checker");
	private File cppChecker = new File("checker.cpp");
	private File graderDir = new File("system");
	private File properties = new File("properties");
	private String prefix;
	private Optional<File> description = Optional.empty();
	private Optional<File> contestantZip = Optional.empty();

	public TaskParser(File dir) {
		prefix = new File(dir, ".").getAbsolutePath();
		if (prefix.endsWith(".")) {
			prefix = prefix.substring(0, prefix.length() - 1);
		}
		taskDir = dir.getAbsoluteFile();
		parseTestsDir();
	}
	
	public Optional<File> getDescription() {
		return description;
	}
	
	public Optional<File> getContestantZip() {
		return contestantZip;
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
	
	public File getGraderDir() {
		return graderDir;
	}
	
	public File getCppChecker() {
		return cppChecker;
	}
	
	public File getProperties() {
		return properties;
	}
	
	public List<File> getSolutions() {
		return solutions;
	}

	public int testsCount() {
		return input.size();
	}

	protected void parseTestsDir() {
		findSolutions();
		findChecker();
		findCppChecker();
		findGrader();
		findProperties();
		findDescription();
		findContestantZip();
		findTests();
	}

	private void findDescription() {
		List<File> filtered = listAllFiles().stream()
				.filter(x -> x.getName().toLowerCase().endsWith("pdf"))
				.collect(Collectors.toList());
		
		if (filtered.size() == 0) return;
		
		for (String s: new String[]{"task.pdf", "description.pdf"}) {
			if (filtered.stream().filter(f -> f.getName().toLowerCase().equals(s)).count() > 0) {
				filtered = filtered.stream().filter(f -> f.getName().toLowerCase().equals(s)).collect(Collectors.toList());
				break;
			}
		}
		
		filtered.sort((a, b) -> a.getAbsolutePath().length() - b.getAbsolutePath().length());
		description = filtered.stream().findFirst();
	}
	
	private void findContestantZip() {
		contestantZip = listAllFiles().stream()
				.filter(x -> x.getName().equalsIgnoreCase("contestant.zip"))
				.sorted((a, b) -> a.getAbsolutePath().length() - b.getAbsolutePath().length())
				.findFirst();
		
		if (!contestantZip.isPresent()) {
			contestantZip = listAllDirs().stream()
					.filter(x -> x.getName().equalsIgnoreCase("contestant"))
					.sorted((a, b) -> b.getAbsolutePath().length() - a.getAbsolutePath().length())
					.map(f -> new File(f, "contestant.zip"))
					.findFirst();
		}
	}
	
	private void findSolutions() {
		solutions = listAllFiles().stream().filter(x -> x.getName().endsWith(".cpp") || x.getName().endsWith(".java"))
				.collect(Collectors.toList());
	}

	private void findChecker() {
		List<File> filtered = listAllFiles().stream()
				.filter(x -> x.getAbsolutePath().contains("checker"))
				.filter(x -> x.getName().endsWith(".jar") || x.getName().endsWith(".sh") || !x.getName().contains("."))
				.collect(Collectors.toList());
		if (filtered.size() == 1) {
			this.checker = filtered.get(0);
			return;
		}
		for (File file : filtered) {
			if (file.getParentFile() == null || !file.getParentFile().getName().equals("checker")) continue;
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
	
	private void findCppChecker() {
		List<File> filtered = listAllFiles().stream()
				.filter(x -> x.getAbsolutePath().contains("checker"))
				.filter(x -> x.getName().endsWith(".cpp"))
				.collect(Collectors.toList());
		if (filtered.size() == 1) {
			this.cppChecker = filtered.get(0);
			return;
		}
		for (File file : filtered) {
			if (file.getParentFile() == null || !file.getParentFile().getName().equals("checker")) continue;
			if (file.getName().equals("checker.cpp")) {
				this.cppChecker = file;
				return;
			}
		}
		for (File file : filtered) {
			if (file.getName().equals("checker.cpp")) {
				this.cppChecker = file;
				return;
			}
		}
	}
	
	private void findGrader() {
		List<File> filtered = listAllFiles().stream()
				.filter(x -> x.getName().equalsIgnoreCase("grader.cpp"))
				.collect(Collectors.toList());
		if (filtered.size() == 1) {
			this.graderDir = filtered.get(0).getParentFile();
		}
	}
	
	private void findProperties() {
		List<File> filtered0 = listAllFiles().stream()
				.filter(x -> x.getAbsolutePath().contains("grade.properties"))
				.collect(Collectors.toList());
		if (filtered0.size() == 1) {
			this.properties = filtered0.get(0);
			return;
		}
		
		List<File> filtered = listAllFiles().stream()
				.filter(x -> x.getAbsolutePath().contains("props")||x.getAbsolutePath().contains("properties"))
				.collect(Collectors.toList());
		if (filtered.size() == 1) {
			this.properties = filtered.get(0);
			return;
		}
		
		List<File> filteredProperties = filtered.stream().filter(x -> x.getName().equals(".properties"))
				.collect(Collectors.toList());
		if (filtered.size() == 1) {
			this.properties = filteredProperties.get(0);
		}
	}
	
	private void findTests() {
		try {
			List<TestCase> tests = new TaskTestsFinderv2().findTests(taskDir.toPath());
			for (TestCase test: tests) {
				input.add(new File(taskDir, test.getInput()));
				output.add(new File(taskDir, test.getOutput()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		List<String> files = listAllFiles().stream().filter(x -> x.isFile()).map(File::getAbsolutePath)
//				.filter(x -> !x.endsWith(".cpp") && !x.endsWith(".java") && !x.endsWith(".jar"))
//				.collect(Collectors.toList());
//
//		int numberOfTests = countTests(files);
//		String bestInputCandidate = findBestInputCandidate(files, numberOfTests);
//
//		for (int i = 1; i <= numberOfTests; i++) {
//			findTestCases(i, files, bestInputCandidate);
//		}
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
		List<String> testCaseCandidates = allCandidates.stream()
				.collect(Collectors.groupingBy(x -> countNumberInString(testCase, x))).entrySet().stream()
				.filter(e -> e.getKey() != 0).sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
				.map(e -> e.getValue()).findFirst().orElse(new ArrayList<String>());
		return testCaseCandidates;
	}

	private List<String> findInputCandidates(List<String> candidates) {
		List<String> inputCandidates = candidates.stream().collect(Collectors.groupingBy(x -> countInSubstrings(x)))
				.entrySet().stream().filter(e -> e.getKey() != 0)
				.sorted(Map.Entry.comparingByKey(Comparator.reverseOrder())).map(e -> e.getValue()).findFirst()
				.orElse(new ArrayList<String>());
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
		if (!dir.getName().startsWith("sandbox_")) {
			Arrays.stream(dir.listFiles()).filter(Objects::nonNull).filter(d -> !d.toString().contains("__MACOSX")).filter(File::isFile).forEach(allFiles::add);
			Arrays.stream(dir.listFiles()).filter(Objects::nonNull).filter(d -> !d.toString().contains("__MACOSX")).filter(File::isDirectory).forEach(x -> listAllFiles(x, allFiles));
		}
	}

	private List<File> listAllDirs() {
		List<File> allDirs = new ArrayList<>();
		listAllDirs(taskDir, allDirs);
		return allDirs;
	}
	
	private void listAllDirs(File dir, List<File> allDirs) {
		if (!dir.getName().startsWith("sandbox_")) {
			Arrays.stream(dir.listFiles()).filter(Objects::nonNull).filter(d -> !d.toString().contains("__MACOSX")).filter(File::isDirectory).forEach(allDirs::add);
			Arrays.stream(dir.listFiles()).filter(Objects::nonNull).filter(d -> !d.toString().contains("__MACOSX")).filter(File::isDirectory).forEach(x -> listAllDirs(x, allDirs));
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
		return (int) Arrays.stream(fileName.replaceAll("[^0-9]", " ").split(" ")).filter(x -> x.length() > 0)
				.map(Integer::valueOf).filter(x -> x == number).count();
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
