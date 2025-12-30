
package org.pesho.grader.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;
import org.pesho.grader.task.parser.CheckerFinder;
import org.pesho.grader.task.parser.ContestantFinder;
import org.pesho.grader.task.parser.CriteriaFinder;
import org.pesho.grader.task.parser.GraderFinder;
import org.pesho.grader.task.parser.ImagesFinder;
import org.pesho.grader.task.parser.ManagerFinder;
import org.pesho.grader.task.parser.PropertiesFinder;
import org.pesho.grader.task.parser.QuizFinder;
import org.pesho.grader.task.parser.SolutionsFinder;
import org.pesho.grader.task.parser.StatementFinder;
import org.pesho.grader.task.parser.TranslationsFinder;
import org.pesho.grader.task.parser.AnalysisFinder;
import org.pesho.grader.task.parser.TaskFilesFinder;
import org.pesho.grader.task.parser.TaskTestsFinderv2;
import org.pesho.grader.task.parser.TaskTestsFinderv3;
import org.pesho.grader.task.parser.TaskTestsFinderv4;
import org.pesho.grader.task.quiz.Quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pesho.grader.task.quiz.QuizTask;

public class TaskDetails {

	private String taskName;
	
	private Map<String, Object> files;
	
	private double points;
	private boolean isDefaultPoints;
	private int precision;
	private int processes;
	private int openFiles;
	private double time;
	private double ioTime;
	private double compileTime;
	private double javaCompileTime;
	private boolean isDefaultCompileTime;
	private int memory;
	private int compileMemory;
	private int javaCompileMemory;
	private boolean isDefaultCompileMemory;
	private int rejudgeTimes;
	private String checker;
	private String cppChecker;
	private String manager;
	private String cppManager;
	private String graderDir;
	private String imagesDir;
	private String feedback;
	private String sample;
	private String groups;
	private String weights;
	private String scoring;
	private String scoringType;
	private String dependencies;
	private List<TestGroup> testGroups;
	private String description;
	private String texMode;
	private Map<String,String> translatedStatements;
	private String analysis;
	private String criteria;
	private Double arbiterDelta;
	private String contestantZip;
	private String extensions;
	private Set<String> allowedExtensions;
	private String blacklist;
	private Set<String> blacklistedWords;
	private boolean isInteractive;
	private boolean isCommunication;
	private String outputOnly;
	private boolean isTranslation;
	private String userTests;
	private String info;
	private int timer;
	private Quiz quiz;
	private Map<String, String> error = new HashMap<>();

	public static final TaskDetails EMPTY = new TaskDetails();
	
	public TaskDetails() {
		setProps(new Properties());
	}
	
	private void setProps(Properties props) {
		this.points = findPointsProps(props);
		this.isDefaultPoints = !props.containsKey("points");
		this.precision = Integer.valueOf(props.getProperty("precision", "-1"));
		this.processes = Integer.valueOf(props.getProperty("processes", "1"));
		this.openFiles = Integer.valueOf(props.getProperty("open_files", "64"));
		this.time = Double.valueOf(props.getProperty("time", "1"));
		this.ioTime = Double.valueOf(props.getProperty("io_time", "0"));
		this.compileTime = findCompileTimeProps(props);
		this.javaCompileTime = Double.valueOf(props.getProperty("java_compile_time", "300"));
		this.isDefaultCompileTime = !props.containsKey("compile_time");
		this.memory = Integer.valueOf(props.getProperty("memory", "256"));
		this.compileMemory = findCompileMemoryProps(props);
		this.isDefaultCompileMemory = !props.containsKey("compile_memory");
		this.javaCompileMemory = Integer.valueOf(props.getProperty("java_compile_memory", "1536"));
		this.rejudgeTimes = Integer.valueOf(props.getProperty("rejudge", "0"));
		this.feedback = props.getProperty("feedback", "FULL").equals("FULL") ? "FULL" : fixSequence(props.getProperty("feedback", "FULL"));
		this.sample = fixSequence(props.getProperty("sample", ""));
		this.groups = fixSequence(props.getProperty("groups", ""));
		this.weights = fixSequence2(props.getProperty("weights", ""));
		this.scoring = props.getProperty("scoring", this.groups.isEmpty()&&!props.containsKey("patterns")?"sum":"min_fast").trim();
		this.scoringType = props.getProperty("scoring_type", this.groups.isEmpty()?"best":(this.weights.isEmpty()?"best":"aggregated")).trim();
		this.extensions = findExtensions(props);
		this.info = props.getProperty("info", "").trim();
		this.dependencies = fixSequence(props.getProperty("dependencies", ""));
		this.texMode = props.getProperty("latex", "lualatex").trim();
		this.allowedExtensions = findAllowedExtensions(extensions);
		this.blacklist = props.getProperty("blacklist", "").trim();
		this.blacklistedWords = Arrays.stream(blacklist.split(",")).map(s -> s.trim()).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
		this.arbiterDelta = Double.valueOf(props.getProperty("arbiter_delta", "1.0"));
		this.timer = Integer.valueOf(props.getProperty("timer", "0"));
		this.userTests = props.getProperty("user_tests", "no").trim();

		this.checker = null;
		this.manager = null;
		this.graderDir = null;
		this.imagesDir = null;
		this.isInteractive = false;
		this.isCommunication = false;
		this.outputOnly = "no";
		this.isTranslation = false;
		this.analysis = null;
		this.description = null;
		this.translatedStatements = new HashMap<>();
		this.contestantZip = null;
		this.testGroups = new ArrayList<>();
		this.files = new HashMap<>();
	}

	public TaskDetails(String taskName, File taskFile) {
		this(taskName, taskFile.toPath());
	}
	
	public TaskDetails(String taskName, Path taskPath) {
		try {
			parseTask(taskName, taskPath);
		} catch (Exception e) {
			error.put("exception", e.getMessage());
			setProps(new Properties());
			try {
				files = TaskFilesFinder.find(taskName, taskPath, Files.walk(taskPath).map(p -> taskPath.relativize(p)).collect(Collectors.toList()));
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	public void parseTask(String taskName, Path taskPath) throws IOException {
		this.taskName = taskName != null ? taskName : taskPath.getFileName().toString();
		
		List<Path> paths = findAllPaths(taskPath);
		
		Properties props = findProperties(taskPath, paths);
		
		CriteriaFinder.find(paths).ifPresent(path -> {
			try {
				File file = new File(taskPath.resolve(path).toString());
				Criteria[] criterias = new ObjectMapper().readValue(file, Criteria[].class);
				this.criteria = new ObjectMapper().writeValueAsString(criterias);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		findErrors(props);
		setProps(props);

        this.checker = findChecker(taskPath, true).map(Path::toString).orElse(null);
        this.manager = ManagerFinder.find(paths).map(Path::toString).orElse(null);
		this.graderDir = GraderFinder.find(paths, allowedExtensions).map(p -> p.getParent()).map(Path::toString).orElse(null);
		this.imagesDir = ImagesFinder.find(paths).map(Path::toString).orElse(null);
        this.isInteractive = graderDir != null;
        this.isCommunication = manager != null;
		this.outputOnly = allowedExtensions.contains("txt") ? "single" : allowedExtensions.contains("zip") ? "multiple" : "no";
		this.isTranslation = allowedExtensions.contains("pdf");
		if (this.outputOnly.equals("multiple") || this.isTranslation) this.userTests = "no";
		else if (!props.containsKey("user_tests")) this.userTests = !this.outputOnly.equals("no") || this.isInteractive || this.isCommunication ? "no" : this.checker != null ? "no_checker" : "unrestricted";
		this.analysis = findAnalysis(paths);
		this.description = findDescription(taskPath, true);
		if (description == null) addError("statement", "backend.no_statement");
		if (description != null && description.endsWith(".tex") && taskPath.resolve(description.replaceAll("\\.tex$", ".pdf")).toFile().exists()) { /// statement should be compiled at this time
			description = description.replaceAll("\\.tex$", ".pdf");
		}
		this.translatedStatements = TranslationsFinder.find(description, paths).stream().map(Path::toString)
			.collect(Collectors.toMap(x -> x.toString().substring(x.length()-6, x.length()-4), x-> x, (key1, key2) -> key1, TreeMap::new));
		if (!translatedStatements.containsKey("en") && description != null) translatedStatements.put("en", description);
		else if (translatedStatements.containsKey("en")) description = translatedStatements.get("en");
		this.contestantZip = ContestantFinder.find(paths).map(Path::toString).orElse(null);

		if ("quiz".equals(scoring)) {
			QuizFinder.find(paths).ifPresent(path -> {
				try {
					File file = new File(taskPath.resolve(path).toString());
					quiz = new ObjectMapper().readValue(file, Quiz.class);
//					System.out.println("quiz: " + quiz + " " + quiz.getTasks().length);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		
		List<TestCase> testCases = new ArrayList<>();
		try {
			testCases = findTestCases(taskPath, true);
		} catch (IllegalStateException e) {
			addError("tests_parse", e.getMessage());
		}
		if (props.containsKey("patterns") && groups.isEmpty()) {
			String[] patternsSplit = props.getProperty("patterns").split(",");
			int total = (testsFromZero() ? 0 : 1);
			for (String patternSplit: patternsSplit) {
				boolean first = true;
				for (TestCase testCase: testCases) {
					if (testCase.getInput().contains(patternSplit)) {
						if (first == false) groups += ";";
						first = false;
						groups += total;
						total++;
					}
				}
				groups += ",";
			}
			groups = groups.substring(0, groups.length()-1);
		}

		Set<Integer> feedbackGroups = feedback();
		TestGroup[] testGroups = null;
		if (testCases.isEmpty()) {
			testGroups = new TestGroup[testCases.size()];
			addError("tests", "backend.no_tests");
		} else if (groups.isEmpty()) {
			if (sampleTests().size() == 0 && testCases.get(0).getNumber() == 0) sample = "0";
			Set<Integer> sampleTests = sampleTests();
			testGroups = new TestGroup[testCases.size()];
			
			double testWeight = 1.0/(testCases.size()-sampleTests.size());
			for (int i = 0; i < testGroups.length; i++) {
				boolean hasFeedback = isFullFeedback() || feedbackGroups.contains(testCases.get(i).getNumber());
				boolean isSample = sampleTests.contains(testCases.get(i).getNumber());
				testGroups[i] = new TestGroup(isSample ? 0 : testWeight, hasFeedback, testCases.get(i));
			}
		} else {
			boolean groupsFromZero = groupsFromZero();
			Set<Integer> sampleGroups = sampleTests();
			String[] groupsSplit = groups.split(",");
			String[] weightsSplit = (weights.isEmpty())?new String[]{}:weights.split(",");
			if (weightsSplit.length != groupsSplit.length && !weights.isEmpty()) addError("weights", "backend.weights_not_match");
			double totalWeight = 0;
			if (weightsSplit.length == groupsSplit.length) {
				for (String weight: weightsSplit) totalWeight += Double.valueOf(weight.trim());
				this.testGroups = new ArrayList<TestGroup>(){{add(new TestGroup(Double.valueOf(weightsSplit[0]), false, new TestCase[]{}));}};
				groupsFromZero = groupsFromZero();
				this.testGroups = new ArrayList<>();
			}
			else {
				for (int i = 0; i < groupsSplit.length; i++) {
					boolean isSample = sampleGroups.contains(i+(groupsFromZero?0:1));
					totalWeight += (isSample?0:1);
				}
			}
			
			boolean testsFromZero = testsFromZero();
			int cnt = 0;
			testGroups = new TestGroup[groupsSplit.length];
			for (int i = 0; i < testGroups.length; i++) {
				String[] tests = groupsSplit[i].split(";");
				cnt += tests.length;
				TestCase[] cases = new TestCase[tests.length];
				for (int j = 0; j < tests.length; j++) {
					Integer t = Integer.valueOf(tests[j]);
					if (testsFromZero) t++;
					if (t < 1 || t > testCases.size()) {
						addError("groups", "backend.invalid_numbers");
						continue;
					}
					cases[j] = testCases.get(t-1);
				}
				
				boolean isSample = sampleGroups.contains(i+(groupsFromZero?0:1));
				double weight = (weightsSplit.length == groupsSplit.length) ? Double.valueOf(weightsSplit[i].trim()) : (isSample?0:1);
				boolean hasFeedback = isFullFeedback() || feedbackGroups.contains(i + (groupsFromZero ? 0 : 1));
				testGroups[i] = new TestGroup(weight/totalWeight, hasFeedback, cases);
			}
			if (cnt != testCases.size()) addError("groups", "backend.groups_not_match");
		}
		this.testGroups = Arrays.asList(testGroups);
		
		if (testGroups.length != 0) {
			boolean fromZero = (testsScoring() && testsFromZero()) || (groupsScoring() && groupsFromZero());
			for (Integer t : sampleTests()) {
				if (fromZero) t++;
				if (t < 1 || t > testGroups.length) addError("samples", "backend.invalid_numbers");
			}
			for (Integer t : feedbackGroups) {
				if (fromZero) t++;
				if (t < 1 || t > testGroups.length) addError("feedback", "backend.invalid_numbers");
			}
		}
		if (dependencies.length() != 0) {
			if (!groupsScoring()) addError("dependencies", "backend.dependencies_not_allowed");
			else {
				if (dependencies.chars().filter(c -> c == ',').count() + 1 != testGroups.length) addError("dependencies", "backend.dependencies_not_match");
				else {
					boolean groupsFromZero = groupsFromZero();
					String[] deps = dependencies.split(",");
					for (int i = 0; i < deps.length; i++) {
						if (deps[i].isEmpty()) continue;
						boolean flag = false;
						for (String g : deps[i].split(";")) {
							try {
								Integer res = Integer.valueOf(g);
								if (groupsFromZero) res++;
								if (res < 1 || res >= (i+1)) {
									addError("dependencies", "backend.invalid_numbers");
									flag = true;
									break;
								}
							} catch (Exception e) {
							}
						}
						if (flag == true) break;
					}
				}
			}
		}

		this.files = TaskFilesFinder.find(taskName, taskPath, Files.walk(taskPath).map(p -> taskPath.relativize(p)).collect(Collectors.toList()));
		
        if (checker != null) ((Map<String, Object>) files.get(checker)).put("type", "checker");
        if (manager != null) ((Map<String, Object>) files.get(manager)).put("type", "manager");
        if (graderDir != null) ((Map<String, Object>) files.get(graderDir)).put("type", "grader");
        if (imagesDir != null) ((Map<String, Object>) files.get(imagesDir)).put("type", "images");
		for (Map.Entry<String,String> entry : translatedStatements.entrySet()) {
			((Map<String, Object>) files.get(entry.getValue())).put("type", "statement_" + entry.getKey());
		}
		if (description != null) ((Map<String, Object>) files.get(description)).put("type", "statement");
		if (analysis != null) ((Map<String, Object>) files.get(analysis)).put("type", "analysis");
	
        PropertiesFinder.find(paths).map(Path::toString).ifPresent(path -> 
        	((Map<String, Object>) files.get(path)).put("type", "props")
        );
        
        SolutionsFinder.find(paths, allowedExtensions).stream().map(Path::toString).forEach(path -> 
        	((Map<String, Object>) files.get(path)).put("type", "solution")
        );
        
//		ContestantFinder.find(paths).map(Path::toString).orElse(null);
		
        for (TestCase testCase: testCases) {
        	((Map<String, Object>) files.get(testCase.getInput())).put("type", "test_in");
        	if (testCase.getOutput() != null) ((Map<String, Object>) files.get(testCase.getOutput())).put("type", "test_out");
        }
        
        if (checker != null) checker = taskPath.resolve(checker).toString();
        if (checker != null && this.checker.toLowerCase().endsWith(".cpp")) {
        	cppChecker = checker;
        	checker = checker.substring(0, this.checker.length()-4);
        }
        if (manager != null) manager = taskPath.resolve(manager).toString();
        if (manager != null && this.manager.toLowerCase().endsWith(".cpp")) {
        	cppManager = manager;
        	manager = manager.substring(0, this.manager.length()-4);
        }
        
        if (graderDir != null) graderDir = taskPath.resolve(graderDir).toString();
        if (contestantZip != null) contestantZip = taskPath.resolve(contestantZip).toString();
        if (description != null) description = taskPath.resolve(description).toString();
		for (Map.Entry<String,String> entry : translatedStatements.entrySet()) {
			translatedStatements.put(entry.getKey(), taskPath.resolve(entry.getValue()).toString());
		}
		if (analysis != null) analysis = taskPath.resolve(analysis).toString();

        if (imagesDir != null) imagesDir = taskPath.resolve(imagesDir).toString();
        
        for (TestCase testCase: testCases) {
        	testCase.setInput(taskPath.resolve(testCase.getInput()).toString());
        	if (testCase.getOutput() != null) testCase.setOutput(taskPath.resolve(testCase.getOutput()).toString());
        }
	}

	private String fixSequence (String sequence) {
		return String.join(",", Arrays.stream(sequence.trim().split(","))
			.map(seq -> String.join(";", Arrays.stream(seq.trim().split(";"))
				.map(t -> t.trim())
				.flatMap(t -> {
					if (!t.contains("-")) return Arrays.asList(t).stream();
					String[] split = t.split("-");
					if (split.length != 2) return Arrays.asList(t).stream();
					try {
						int st = Integer.valueOf(split[0]), end = Integer.valueOf(split[1]);
						String[] nums = new String[end-st+1];
						for (int i = st; i <= end; i++) {
							nums[i-st] = String.valueOf(i);
						}
						return Arrays.stream(nums);
					} catch (Exception e) {
						return Arrays.asList(t).stream();
					}
				})
				.filter(t -> {
					try {
						Integer.valueOf(t);
						return true;
					} catch (Exception e) {
						return false;
					}
				})
				.sorted((l, r) -> Integer.valueOf(l).compareTo(Integer.valueOf(r)))
				.toArray(CharSequence[]::new))
			)
			.toArray(CharSequence[]::new)
		);
	}

	private String fixSequence2 (String sequence) {
		return String.join(",", Arrays.stream(sequence.trim().split(","))
			.map(t -> t.trim())
			.filter(t -> {
				try {
					Double.valueOf(t);
					return true;
				} catch (Exception e) {
					return false;
				}
			})
			.toArray(CharSequence[]::new)
		);
	}

	private void findErrors (Properties props) {
		String naturalNumber = "^[1-9]\\d*$";
		String decimalNumber = "^(?!0+(\\.0+)?$)\\d*\\.?\\d+$";
		
		if (!props.getProperty("points", "100.0").matches(decimalNumber)) addError("points_property", "backend.not_decimal");
		if (!props.getProperty("precision", "-1").matches("^-1$|^0$|" + naturalNumber)) addError("precision_property", "");
		if (!props.getProperty("processes", "1").matches(naturalNumber)) addError("processes_property", "backend.not_natural");
		if (!props.getProperty("open_files", "64").matches(naturalNumber)) addError("open_files_property", "backend.not_natural");
		if (!props.getProperty("time", "1").matches(decimalNumber)) addError("time_property", "backend.not_decimal");
		if (!props.getProperty("io_time", "0").matches("^0$|" + decimalNumber)) addError("io_time_property", "");
		if (!props.getProperty("compile_time", "10").matches(decimalNumber)) addError("compile_time_property", "backend.not_decimal");
		if (!props.getProperty("java_compile_time", "300").matches(decimalNumber)) addError("java_compile_time_property", "backend.not_decimal");
		if (!props.getProperty("memory", "256").matches(naturalNumber)) addError("memory_property", "backend.not_natural");
		if (!props.getProperty("compile_memory", "512").matches(naturalNumber)) addError("compile_memory_property", "backend.not_natural");
		if (!props.getProperty("java_compile_memory", "1536").matches(naturalNumber)) addError("java_compile_memory_property", "backend.not_natural");
		if (!props.getProperty("rejudge", "1").matches(naturalNumber)) addError("rejudge_property", "backend.not_natural");
		if (checkSequence(fixSequence(props.getProperty("feedback", "")), "feedback") != null) addError("feedback_property", checkSequence(fixSequence(props.getProperty("feedback", "")), "feedback"));
		if (checkSequence(fixSequence(props.getProperty("sample", "")), "sample") != null) addError("sample_property", checkSequence(fixSequence(props.getProperty("sample", "")), "sample"));
		if (checkSequence(fixSequence(props.getProperty("groups", "")), "groups") != null) addError("groups_property", checkSequence(fixSequence(props.getProperty("groups", "")), "groups"));
		if (!fixSequence2(props.getProperty("weights", "1")).matches("^\\d+(\\.\\d+)?(,\\d+(\\.\\d+)?)*$")) addError("weights_property", "");
		if (Arrays.stream(props.getProperty("scoring", "").trim().split(",")).anyMatch(p -> Arrays.asList("", "sum", "min", "min_fast").stream().noneMatch(s -> p.trim().equalsIgnoreCase(s)))) addError("scoring_property", "");
		if (Arrays.asList("", "best", "aggregated").stream().noneMatch(s -> props.getProperty("scoring_type", "").trim().equalsIgnoreCase(s))) addError("scoring_type_property", "");
		if (Arrays.stream(props.getProperty("extensions", "cpp").trim().split(",")).anyMatch(p -> Arrays.asList("cpp", "c", "h", "java", "py", "go", "cs", "zip", "txt", "pdf").stream().noneMatch(s -> p.trim().equalsIgnoreCase(s)))) addError("extensions_property", "");
		if (checkSequence(fixSequence(props.getProperty("dependencies", "")), "dependencies") != null) addError("dependencies_property", checkSequence(fixSequence(props.getProperty("dependencies", "")), "dependencies"));
		if (Arrays.asList("lualatex", "none", "pdflatex", "xelatex").stream().noneMatch(s -> props.getProperty("latex", "lualatex").trim().equalsIgnoreCase(s))) addError("latex_property", "");
		if (Arrays.asList("no", "no_checker", "unrestricted").stream().noneMatch(s -> props.getProperty("user_tests", "no").trim().equalsIgnoreCase(s))) addError("user_tests_property", "");
	}

	private String checkSequence (String sequence, String type) {
		String regex = "^(\\d*)?([,;](\\d*)?)*$";
		if (!sequence.matches(regex)) return "";
		String[] tokens = sequence.split(",");
		if (type.equals("dependencies")) {
			for (String token : tokens) {
				String[] groups = token.split(";");
				if (Arrays.stream(groups).filter(g -> !g.isEmpty()).collect(Collectors.toList()).size() != Arrays.stream(groups).filter(g -> !g.isEmpty()).collect(Collectors.toSet()).size()) return "backend.sequence_repeat";
			}
			return null;
		}
		if (Arrays.stream(tokens).flatMap(t -> Arrays.stream(t.split(";"))).filter(g -> !g.isEmpty()).collect(Collectors.toList()).size() != 
			Arrays.stream(tokens).flatMap(t -> Arrays.stream(t.split(";"))).filter(g -> !g.isEmpty()).collect(Collectors.toSet()).size()) return "backend.sequence_repeat";
		return null;
	}

	private static List<Path> findAllPaths (Path taskPath) throws IOException {
		return Files.walk(taskPath)
				.filter(p -> !p.toString().contains("__MACOSX"))
//				.filter(Files::isRegularFile)
				.map(p -> taskPath.relativize(p))
//				.map(Path::toString)
				.collect(Collectors.toList());
	}

	private static Properties findProperties (Path taskPath, List<Path> paths) {
		Properties props = new Properties();
		PropertiesFinder.find(paths).ifPresent(path -> {
			try (FileInputStream fileInputStream = new FileInputStream(taskPath.resolve(path).toString())) {
				props.load(fileInputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return props;
	}

	public void setPoints(double points) {
		this.points = points;
	}
	
	public double getPoints() {
		return points;
	}

	public boolean isDefaultPoints() {
		return isDefaultPoints;
	}

	public static Double findPointsProps(Properties props) {
		return Double.valueOf(props.getProperty("points", "100.0"));
	}

	public static Double findPoints(Path taskPath) throws IOException {
		List<Path> paths = findAllPaths(taskPath);
		Properties props = findProperties(taskPath, paths);	
		return findPointsProps(props);
	}
	
	public void setPrecision(int precision) {
		this.precision = precision;
	}
	
	public int getPrecision() {
		return precision!=-1?precision:0;
	}
	
	public void setProcesses(int processes) {
		this.processes = processes;
	}
	
	public int getProcesses() {
		return processes;
	}

	public void setOpenFiles(int openFiles) {
		this.openFiles = openFiles;
	}

	public int getOpenFiles() {
		return openFiles;
	}

	public void setTime(double time) {
		this.time = time;
	}
	
	public double getTime() {
		return time;
	}

	public void setIoTime(double ioTime) {
		this.ioTime = ioTime;
	}
	
	public double getIoTime() {
		return ioTime;
	}

	public void setCompileTime(double compileTime) {
		this.compileTime = compileTime;
	}
	
	public Map<String, Double> getCompileTime() {
		Map<String, Double> tmp = new HashMap<>();
		tmp.put("default", compileTime);
		tmp.put("java", javaCompileTime);
		return tmp;
	}

	public void setJavaCompileTime(double javaCompileTime) {
		this.javaCompileTime = javaCompileTime;
	}

	public boolean isDefaultCompileTime() {
		return isDefaultCompileTime;
	}

	public static Double findCompileTimeProps(Properties props) {
		return Double.valueOf(props.getProperty("compile_time", "10"));
	}

	public static Double findCompileTime(Path taskPath) throws IOException {
		List<Path> paths = findAllPaths(taskPath);
		Properties props = findProperties(taskPath, paths);	
		return findCompileTimeProps(props);
	}
	
	public void setMemory(int memory) {
		this.memory = memory;
	}
	
	public int getMemory() {
		return memory;
	}

	public void setCompileMemory(int compileMemory) {
		this.compileMemory = compileMemory;
	}
	
	public Map<String, Integer> getCompileMemory() {
		Map<String, Integer> tmp = new HashMap<>();
		tmp.put("default", compileMemory);
		tmp.put("java", javaCompileMemory);
		return tmp;
	}

	public void setJavaCompileMemory(int javaCompileMemory) {
		this.javaCompileMemory = javaCompileMemory;
	}

	public boolean isDefaultCompileMemory() {
		return isDefaultCompileMemory;
	}

	public static Integer findCompileMemoryProps(Properties props) {
		return Integer.valueOf(props.getProperty("compile_memory", "512"));
	}

	public static Integer findCompileMemory(Path taskPath) throws IOException {
		List<Path> paths = findAllPaths(taskPath);
		Properties props = findProperties(taskPath, paths);	
		return findCompileMemoryProps(props);
	}


	public int getRejudgeTimes() {
		return rejudgeTimes;
	}
	
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	
	public String getFeedback() {
		return feedback;
	}
	
	public void setSample(String sample) {
		this.sample = sample;
	}
	
	public String getSample() {
		return sample;
	}
	
	public void setGroups(String groups) {
		this.groups = groups;
	}
	
	public String getGroups() {
		return groups;
	}
	
	public void setWeights(String weights) {
		this.weights = weights;
	}
	
	public String getWeights() {
		return weights;
	}
	
	public void setScoring(String scoring) {
		this.scoring = scoring;
	}
	
	public String getScoring() {
		return scoring;
	}

	public void setScoringType(String scoringType) {
		this.scoringType = scoringType;
	}

	public String getScoringType() {
		return scoringType;
	}

	
	public String getDependencies() {
		return dependencies;
	}
	
	public boolean groupsFromZero() {
		if (sampleTests().contains(0)) return true;
		if (feedback().contains(0)) return true;
		if (Arrays.stream(dependencies.split(",")).anyMatch(d -> Arrays.stream(d.split(";")).anyMatch(g -> g.equals("0")))) return true;
		String[] deps = dependencies.split(",");
		for (int i = 0; i < deps.length; i++) {
			int maxGroup = Arrays.stream(deps[i].split(";")).filter(g -> !g.isEmpty()).mapToInt(Integer::parseInt).max().orElse(-1);
			if (maxGroup == i) return false; /// backward compatability
		}
		if (!sample.isEmpty()) return false;
		return (getTestGroups().size() > 0 && getTestGroups().get(0).getWeight() == 0);
	}

	public List<Integer> dependsOn(int groupNumber) {
		if (dependencies.split(",").length < groupNumber) return new ArrayList<>();

		boolean number0 = groupsFromZero();
		
		String group = dependencies.split(",",-1)[groupNumber-1];
		if (group.isEmpty()) return new ArrayList<>();
		return Arrays.stream(group.split(";")).map(g -> {
			int res = Integer.parseInt(g);
			if (number0) res++;
			return res;
		}).collect(Collectors.toList());
	}
	
	public void setChecker(String checker) {
		this.checker = checker;
	}
	
	public String getChecker() {
		return checker;
	}
	
	public String getCppChecker() {
		return cppChecker;
	}

	public static Optional<Path> findChecker(Path taskPath, boolean relative) throws IOException {
		List<Path> paths = findAllPaths(taskPath);
		return CheckerFinder.find(paths).map(c -> (relative == false ? taskPath.resolve(c) : c));
	}
	
	public String getGraderDir() {
		return graderDir;
	}
	
	public void setManager(String manager) {
		this.manager = manager;
	}
	
	public String getManager() {
		return manager;
	}
	
	public void setCppManager(String cppManager) {
		this.cppManager = cppManager;
	}
	
	public String getCppManager() {
		return cppManager;
	}
	
	public void setTestGroups(List<TestGroup> testGroups) {
		this.testGroups = testGroups;
	}
	
	public List<TestGroup> getTestGroups() {
		return testGroups;
	}

	private static boolean testsFromZero(boolean testsScoring, boolean groupsScoring, TreeSet<Integer> samples, TreeSet<Integer> feedback, String groups, List<TestGroup> testGroups) {
		if (testsScoring && samples.contains(0)) return true;
		if (testsScoring && feedback.contains(0)) return true;
		if (groupsScoring && Arrays.stream(groups.split(",")).anyMatch(g -> Arrays.stream(g.split(";")).anyMatch(t -> t.equals("0")))) return true;
		return (testGroups.size() > 0 && testGroups.get(0).getTestCases().size() > 0 && testGroups.get(0).getTestCases().get(0).getNumber() == 0);
	}

	public boolean testsFromZero() {
		return testsFromZero(testsScoring(), groupsScoring(), sampleTests(), feedback(), getGroups(), getTestGroups());
	}

	public static List<TestCase> findTestCases(Path taskPath, boolean relative) throws IOException, IllegalStateException {
		//if ("manual".equals(scoring) || "quiz".equals(scoring)) new ArrayList<>();
		List<Path> paths = findAllPaths(taskPath);
		Properties props = findProperties(taskPath, paths);
		List<TestCase> testCases;
		if (props.containsKey("patterns") || (props.containsKey("input") && props.containsKey("output"))) {
			String scoring = props.getProperty("scoring", "");
			String groups = props.getProperty("groups", "");
			boolean testsFromZero = testsFromZero(findTestsScoring(scoring, groups), findGroupsScoring(scoring, groups), findSampleTests(props.getProperty("sample", "")), findFeedback(props.getProperty("feedback", "")), groups, new ArrayList<>());
			if (props.containsKey("patterns")) testCases = TaskTestsFinderv4.find(paths, taskPath, props.getProperty("patterns"), testsFromZero);
			else testCases = TaskTestsFinderv3.find(paths, taskPath, props.getProperty("input"), props.getProperty("output"), testsFromZero);
		}
		else testCases = TaskTestsFinderv2.find(paths, taskPath, findChecker(taskPath, relative).isPresent());
		if (relative == false) {
			for (TestCase testCase : testCases) {
				testCase.setInput(taskPath.resolve(testCase.getInput()).toString());
				if (testCase.getOutput() != null) testCase.setOutput(taskPath.resolve(testCase.getOutput()).toString());
			}
		}
		return testCases;
	}
	
	private static boolean propertyContainsToken(String property, String token) {
		return Arrays.stream(property.trim().split(",")).anyMatch(t -> t.trim().equalsIgnoreCase(token));
	}

	private static boolean findTestsScoring(String scoring, String groups) {
		return (propertyContainsToken(scoring, "sum") && groups.isEmpty()) || propertyContainsToken(scoring, "tests") || propertyContainsToken(scoring, "icpc"); // backward compatability
	}

	public boolean testsScoring() {
		return findTestsScoring(getScoring(), getGroups());
	}

	private static boolean findGroupsScoring(String scoring, String groups) {
		return !findTestsScoring(scoring, groups);
	}

	public boolean groupsScoring() {
		return findGroupsScoring(getScoring(), getGroups());
	}
	
	public boolean sumScoring() {
		return propertyContainsToken(scoring, "sum");
	}

	public boolean minScoring() {
		return propertyContainsToken(scoring, "min") || propertyContainsToken(scoring, "min_fast");
	}
	
	public boolean stopScoringOnFailure() {
		return propertyContainsToken(scoring, "min_fast") || !propertyContainsToken(scoring, "min");
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public static String findDescription (Path taskPath, boolean relative) throws IOException {
		List<Path> paths = findAllPaths(taskPath);
		String analysis = findAnalysis(paths);
		String description = StatementFinder.find(analysis, paths, taskPath).map(Path::toString).orElse(null);
		if (description == null) return null;
		if (relative == false) return taskPath.resolve(description).toString();
		return description;
	}

	public String getTexMode() {
		return texMode;
	}
	
	public void setTexMode(String texMode) {
		this.texMode = texMode;
	}

	public static String findTexMode (Path taskPath) throws IOException {
		List<Path> paths = findAllPaths(taskPath);
		Properties props = findProperties(taskPath, paths);	
		return props.getProperty("latex", "lualatex").trim();
	}

	public Map<String, String> getTranslations() {
		return translatedStatements;
	}

	public String getAnalysis() {
		return analysis;
	}

	public void setAnalysis (String analysis) {
		this.analysis = analysis;
	}

	public static String findAnalysis (List<Path> paths) {
		return AnalysisFinder.find(paths).map(Path::toString).orElse(null);
	}

	public String getImagesDir() {
		return imagesDir;
	}
	
	public String getCriteria() {
		return criteria;
	}

	public Double getArbiterDelta() {
		return arbiterDelta;
	}

	public String getContestantZip() {
		return contestantZip;
	}
	
	public void setContestantZip(String contestantZip) {
		this.contestantZip = contestantZip;
	}
	
	public boolean isInteractive() {
		return isInteractive;
	}
	
	public boolean isCommunication() {
		return isCommunication;
	}

	public String outputOnly() {
		return outputOnly;
	}

	public boolean isTranslation() {
		return isTranslation;
	}

	public void setUserTests(String userTests) {
		this.userTests = userTests;
	}
	
	public String getUserTests() {
		return userTests;
	}
	
	public boolean hasFilesToDownload() {
		return contestantZip != null;
	}
	
	public boolean isPartial() {
		return precision != -1;
	}
	
	public Set<String> getAllowedExtensions() {
		return allowedExtensions;
	}

	private static String findExtensions(Properties props) {
		return props.getProperty("extensions", "cpp").trim();
	}

	private static Set<String> findAllowedExtensions(String extensions) {
		return Arrays.stream(extensions.split(",")).map(s -> s.trim()).collect(Collectors.toSet());
	}

	public static List<Path> findFilesAllowedExtensions(Path taskPath, boolean relative) throws IOException {
		List<Path> paths = findAllPaths(taskPath);
		Properties props = findProperties(taskPath, paths);
		Set<String> allowedExtensions = findAllowedExtensions(findExtensions(props));
		return paths.stream()
			.filter(p -> allowedExtensions.stream().anyMatch(e -> {
				return p.getFileName().toString().toLowerCase().endsWith("."+e.toLowerCase());
			}))
			.map(p -> (relative == false ? taskPath.resolve(p) : p))
			.collect(Collectors.toList());
	}
	
	public Set<String> getBlacklistedWords() {
		return blacklistedWords;
	}
	
	private static boolean isFullFeedback(String feedback) {
		return feedback.trim().equalsIgnoreCase("full");
	}

	public boolean isFullFeedback() {
		return isFullFeedback(getFeedback());
	}
	
	public double getPublicScore() {
		if (isFullFeedback()) return Precision.round(getPoints(), getPrecision());
		
		boolean fromZero = (testsScoring() && testsFromZero()) || (groupsScoring() && groupsFromZero());
		TreeSet<Integer> feedback = feedback();
		double publicWeight = 0.0;
		for (int i = 0; i < getTestGroups().size(); i++) {
			if (feedback.contains(i + (fromZero ? 0 : 1))) {
				publicWeight += getTestGroups().get(i).getWeight();
			}
		}
		return Precision.round(getPoints()*publicWeight, getPrecision());
	}
	
	private static TreeSet<Integer> findFeedback(String feedback) {
		if (isFullFeedback(feedback)) return new TreeSet<>();
		return new TreeSet<Integer> (
			Arrays.stream(feedback.split(","))
			.filter(f -> !f.isEmpty())
			.flatMap(f -> Arrays.stream(f.split(";")))
			.map(f -> Integer.valueOf(f))
			.collect(Collectors.toSet())
		);
	}

	public TreeSet<Integer> feedback() {
		return findFeedback(getFeedback());
	}
	
	private static TreeSet<Integer> findSampleTests(String sample) {
		return new TreeSet<Integer> (
			Arrays.stream(sample.split(","))
			.filter(s -> !s.isEmpty())
			.flatMap(s -> Arrays.stream(s.split(";")))
			.map(s -> Integer.valueOf(s))
			.collect(Collectors.toSet())
		);
	}

	public TreeSet<Integer> sampleTests() {
		return findSampleTests(getSample());
	}
	
	public void setFiles(Map<String, Object> files) {
		this.files = files;
	}
	
	public Map<String, Object> getFiles() {
		return files;
	}
	
	public String getTaskName() {
		return taskName;
	}
	
	public String getInfo() {
		return info;
	}
	
	public Map<String, String> getError() {
		return error;
	}

	public void addError (String type, String newError) {
		error.put(type, newError);
	}
	
	public boolean isManualScoring() {
		return "manual".equals(scoring);
	}

	public boolean isQuiz() {
		return "quiz".equals(scoring);
	}

	public Quiz getQuiz(Integer seed) {
		if (seed == null) return quiz;

		Quiz seededQuiz = new Quiz();
		String option = "1-" + quiz.getTasks().length;
		if (quiz.getOptions() != null && quiz.getOptions().length != 0) {
			option = quiz.getOptions()[new Random(seed).nextInt(quiz.getOptions().length)];
		}
		String[] split = option.split("-");
		int first = Integer.parseInt(split[0]);
		int last = Integer.parseInt(split[1]);
		QuizTask[] tasks = new QuizTask[last-first+1];
		for (int i = first; i <= last; i++) {
			QuizTask quizTask = quiz.getTasks()[i-1].clone();
			tasks[i-first] = quizTask;
		}
		seededQuiz.setTasks(tasks);

		return seededQuiz;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}

	public int getTimer() {
		return timer;
	}
	
	
}

