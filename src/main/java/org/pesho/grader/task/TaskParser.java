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
	private File checker = new File("checker");
	
	public TaskParser(File dir) {
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
	
	public int testsCount() {
		return input.size();
	}
	
	protected void parseTestsDir() {
		findChecker();
		
		List<String> files = Arrays.stream(taskDir.listFiles())
				.filter(x -> x.isFile())
				.map(File::getName)
				.filter(x -> !x.endsWith(".cpp") && !x.endsWith(".java") && !x.endsWith(".jar"))
				.collect(Collectors.toList());
		for (int i = 0; i <= 20; i++) {
			findTestCases(i, files);
		}
	}
	
	private void findChecker() {
		List<String> filtered = 
				Arrays.stream(taskDir.listFiles()).collect(Collectors.toList())
				.stream()
				.map(x -> x.getName())
				.filter(x -> x.contains("checker"))
				.filter(x -> x.endsWith(".jar") || x.endsWith(".sh") || !x.contains("."))
				.collect(Collectors.toList());
		if (filtered.size() == 1) {
			this.checker = new File(taskDir, filtered.get(0));
		} else if (filtered.contains("checker")) {
			this.checker = new File(taskDir, "checker");
		} else if (filtered.contains("checker.sh")) {
			this.checker = new File(taskDir, "checker.sh");
		} else if (filtered.contains("checker.jar")) {
			this.checker = new File(taskDir, "checker.jar");
		}
	}

	public void findTestCases(int testCaseNumber, List<String> fileNames) {
		System.out.println(testCaseNumber);
		int max = fileNames.stream().map(x -> countNumberInString(testCaseNumber, x)).max(Integer::compare).get();
		if (max == 0) return;
		List<String> candidates = fileNames.stream().filter(x -> countNumberInString(testCaseNumber, x) == max).collect(Collectors.toList());

		System.out.println(candidates);

		int maxInCount = candidates.stream().map(x -> substringCount(x, "in")).max(Integer::compare).get();
		
		List<String> inCandidates = candidates.stream().filter(x -> substringCount(x, "in") == maxInCount).collect(Collectors.toList());

		if (inCandidates.size() > 1) {
			throw new IllegalStateException("Cannot select input file from " + inCandidates);
		}
		
		String inputFile = inCandidates.get(0);
		candidates.remove(inputFile);

		int minDifference = candidates.stream().map(x -> LevenshteinDistance.computeLevenshteinDistance(x, inputFile)).min(Integer::compare).get();
		List<String> outCandidates = candidates.stream().filter(x -> LevenshteinDistance.computeLevenshteinDistance(x, inputFile) == minDifference).collect(Collectors.toList());

		if (outCandidates.size() > 1) {
			throw new IllegalStateException("Cannot select output file from " + inCandidates);
		}
		
		String outputFile = outCandidates.get(0);
		input.add(new File(taskDir, inputFile));
		output.add(new File(taskDir, outputFile));
	}

	public int countNumberInString(int number, String fileName) {
		String s = fileName + ".";
		int count = 0;
		int current = -1;
		for (char c: s.toCharArray()) {
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
