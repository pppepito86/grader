
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
import org.pesho.grader.task.parser.TaskFilesFinder;
import org.pesho.grader.task.parser.TaskTestsFinderv2;
import org.pesho.grader.task.parser.TaskTestsFinderv3;
import org.pesho.grader.task.parser.TaskTestsFinderv4;
import org.pesho.grader.task.quiz.Quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pesho.grader.task.quiz.QuizTask;
import org.pesho.grader.task.quiz.QuizType;

public class TaskDetails {

	private String taskName;
	private String taskDir;
	
	private Map<String, Object> files;
	
	private double points;
	private int precision;
	private int processes;
	private double time;
	private double ioTime;
	private int memory;
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
	private String criteria;
	private Double arbiterDelta;
	private String contestantZip;
	private String extensions;
	private Set<String> allowedExtensions;
	private String blacklist;
	private Set<String> blacklistedWords;
	private boolean isInteractive;
	private boolean isCommunication;
	private String info;
	private int timer;
	private Quiz quiz;
	private String error;

	public static final TaskDetails EMPTY = new TaskDetails();
	
	public TaskDetails() {
		setProps(new Properties(), null);
	}
	
	private void setProps(Properties props, String checker, TestGroup... testGroups) {
        this.points = Double.valueOf(props.getProperty("points", "100"));
		this.precision = Integer.valueOf(props.getProperty("precision", "-1"));
		this.processes = Integer.valueOf(props.getProperty("processes", "1"));
        this.time = Double.valueOf(props.getProperty("time", "1"));
        this.ioTime = Double.valueOf(props.getProperty("io_time", "0"));
        this.memory = Integer.valueOf(props.getProperty("memory", "256"));
		this.rejudgeTimes = Integer.valueOf(props.getProperty("rejudge", "1"));
        this.feedback = props.getProperty("feedback", "FULL").trim();
        this.sample = props.getProperty("sample", "").trim();
        this.groups = props.getProperty("groups", "").trim();
        this.weights = props.getProperty("weights", "").trim();
        this.scoring = props.getProperty("scoring", this.groups.isEmpty()?"tests":"min_fast").trim();
        this.scoringType = props.getProperty("scoring_type", this.groups.isEmpty()?"best":(this.weights.isEmpty()?"best":"aggregated")).trim();
        this.extensions = props.getProperty("extensions", "cpp").trim();
        this.info = props.getProperty("info", "").trim();
        this.dependencies = props.getProperty("dependencies", "").trim();
		this.allowedExtensions = Arrays.stream(extensions.split(",")).map(s -> s.trim()).collect(Collectors.toSet());
		this.blacklist = props.getProperty("blacklist", "").trim();
		this.blacklistedWords = Arrays.stream(blacklist.split(",")).map(s -> s.trim()).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        this.checker = checker;
        this.testGroups = new ArrayList<>();
		this.timer = Integer.valueOf(props.getProperty("timer", "0"));
	}

	public TaskDetails(String taskName, File taskFile) {
		this(taskName, taskFile.toPath());
	}
	
	public TaskDetails(String taskName, Path taskPath) {
		try {
			parseTask(taskName, taskPath);
		} catch (Exception e) {
			error = e.getMessage();
			setProps(new Properties(), null);
			try {
				files = TaskFilesFinder.find(taskName, taskPath, Files.walk(taskPath).map(p -> taskPath.relativize(p)).collect(Collectors.toList()));
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	public void parseTask(String taskName, Path taskPath) throws IOException {
		this.taskName = taskName != null ? taskName : taskPath.getFileName().toString();
		this.taskDir = taskPath.toAbsolutePath().toString();
		
		List<Path> paths = Files.walk(taskPath)
				.filter(p -> !p.toString().contains("__MACOSX"))
//				.filter(Files::isRegularFile)
				.map(p -> taskPath.relativize(p))
//				.map(Path::toString)
				.collect(Collectors.toList());
		
		Properties props = new Properties();
		
		PropertiesFinder.find(paths).ifPresent(path -> {
			try (FileInputStream fileInputStream = new FileInputStream(taskPath.resolve(path).toString())) {
				props.load(fileInputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		CriteriaFinder.find(paths).ifPresent(path -> {
			try {
				File file = new File(taskPath.resolve(path).toString());
				Criteria[] criterias = new ObjectMapper().readValue(file, Criteria[].class);
				this.criteria = new ObjectMapper().writeValueAsString(criterias);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		this.points = Double.valueOf(props.getProperty("points", "100.0"));
		this.precision = Integer.valueOf(props.getProperty("precision", "-1"));
		this.processes = Integer.valueOf(props.getProperty("processes", "1"));
		this.time = Double.valueOf(props.getProperty("time", "1"));
        this.ioTime = Double.valueOf(props.getProperty("io_time", "0"));
		this.memory = Integer.valueOf(props.getProperty("memory", "256"));
		this.rejudgeTimes = Integer.valueOf(props.getProperty("rejudge", "1"));
		this.feedback = props.getProperty("feedback", "FULL").trim();
        this.sample = props.getProperty("sample", "").trim();
		this.groups = props.getProperty("groups", "").trim();
        this.weights = props.getProperty("weights", "").trim();
        this.scoring = props.getProperty("scoring", this.groups.isEmpty()&&!props.containsKey("patterns")?"tests":"min_fast").trim();
        this.scoringType = props.getProperty("scoring_type", this.groups.isEmpty()?"best":(this.weights.isEmpty()?"best":"aggregated")).trim();
        this.extensions = props.getProperty("extensions", "cpp").trim();
        this.info = props.getProperty("info", "").trim();
        this.dependencies = props.getProperty("dependencies", "").trim();
        this.allowedExtensions = Arrays.stream(extensions.split(",")).map(s -> s.trim()).collect(Collectors.toSet());
        this.blacklist = props.getProperty("blacklist", "").trim();
		this.blacklistedWords = Arrays.stream(blacklist.split(",")).map(s -> s.trim()).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
		this.arbiterDelta = Double.valueOf(props.getProperty("arbiter_delta", "1.0"));
		this.timer = Integer.valueOf(props.getProperty("timer", "0"));

        this.checker = CheckerFinder.find(paths).map(Path::toString).orElse(null);
        this.manager = ManagerFinder.find(paths).map(Path::toString).orElse(null);
		this.graderDir = GraderFinder.find(paths, allowedExtensions).map(p -> p.getParent()).map(Path::toString).orElse(null);
		this.imagesDir = ImagesFinder.find(paths).map(Path::toString).orElse(null);
        this.isInteractive = graderDir != null;
        this.isCommunication = manager != null;
		this.description = StatementFinder.find(paths).map(Path::toString).orElse(null);
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
		
		List<TestCase> testCases = null;
		if ("manual".equals(scoring) || "quiz".equals(scoring)) {
			testCases = new ArrayList<>();
		} else if (props.containsKey("patterns")) {
			testCases = TaskTestsFinderv4.find(paths, props.getProperty("patterns"));
			if (this.groups == "") {
				String[] patternsSplit = props.getProperty("patterns").split(",");
				int total = 0;
				for (String patternSplit: patternsSplit) {
					int br = 0;
					for (TestCase testCase: testCases) {
						if (testCase.getInput().contains(patternSplit)) br++;
					}
					total+=br;
					groups += (total-br+1)+"-"+total+ ",";
				}
				groups = groups.substring(0, groups.length()-1);
			}
		} else if (props.containsKey("input") && props.containsKey("output")) {
			testCases = new TaskTestsFinderv3().find(paths, props.getProperty("input"), props.getProperty("output"));
		} else {
			testCases = TaskTestsFinderv2.find(paths);
		}

		Set<Integer> feedbackGroups = feedback();
		TestGroup[] testGroups = null;
		if (testCases.isEmpty()) {
			testGroups = new TestGroup[testCases.size()];
		} else if (groups.isEmpty()) {
			Set<Integer> sampleTests = sampleTests();
			testGroups = new TestGroup[testCases.size()];
			
			double testWeight = 1.0/(testCases.size()-sampleTests.size());
			for (int i = 0; i < testGroups.length; i++) {
				boolean hasFeedback = isFullFeedback() || feedbackGroups.contains(i+1);
				boolean isSample = sampleTests.contains(i+1);
				testGroups[i] = new TestGroup(isSample?0:testWeight, hasFeedback, testCases.get(i));
			}
		} else {
			String[] groupsSplit = groups.split(",");
			String[] weightsSplit = weights.split(",");
			double totalWeight = 0;
			if (!weights.trim().isEmpty()) {
				for (String weight: weightsSplit) totalWeight += Double.valueOf(weight.trim());
			}
			if (totalWeight == 0) totalWeight = groupsSplit.length;
			
			testGroups = new TestGroup[groupsSplit.length];
			for (int i = 0; i < testGroups.length; i++) {
				String[] s = groupsSplit[i].trim().split("-");
				int first = Integer.valueOf(s[0]);
				int last = Integer.valueOf(s[1]);
				TestCase[] cases = new TestCase[last-first+1];
				for (int j = first; j <= last; j++) {
					cases[j-first] = testCases.get(j-1);
				}
				
				double weight = (weightsSplit.length == groupsSplit.length) ? Double.valueOf(weightsSplit[i].trim()) : 1;
				boolean hasFeedback = isFullFeedback() || feedbackGroups.contains(i+1);
				testGroups[i] = new TestGroup(weight/totalWeight, hasFeedback, cases);
			}
		}
		this.testGroups = Arrays.asList(testGroups);

		this.files = TaskFilesFinder.find(taskName, taskPath, Files.walk(taskPath).map(p -> taskPath.relativize(p)).collect(Collectors.toList()));
		
        if (checker != null) ((Map<String, Object>) files.get(checker)).put("type", "checker");
        if (manager != null) ((Map<String, Object>) files.get(manager)).put("type", "manager");
        if (graderDir != null) ((Map<String, Object>) files.get(graderDir)).put("type", "grader");
        if (imagesDir != null) ((Map<String, Object>) files.get(imagesDir)).put("type", "images");
        if (description != null) ((Map<String, Object>) files.get(description)).put("type", "statement");
        PropertiesFinder.find(paths).map(Path::toString).ifPresent(path -> 
        	((Map<String, Object>) files.get(path)).put("type", "props")
        );
        
        SolutionsFinder.find(paths, allowedExtensions).stream().map(Path::toString).forEach(path -> 
        	((Map<String, Object>) files.get(path)).put("type", "solution")
        );
        
//		ContestantFinder.find(paths).map(Path::toString).orElse(null);
		
        for (TestCase testCase: testCases) {
        	((Map<String, Object>) files.get(testCase.getInput())).put("type", "test_in");
        	((Map<String, Object>) files.get(testCase.getOutput())).put("type", "test_out");
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
        if (imagesDir != null) imagesDir = taskPath.resolve(imagesDir).toString();
        
        for (TestCase testCase: testCases) {
        	testCase.setInput(taskPath.resolve(testCase.getInput()).toString());
        	testCase.setOutput(taskPath.resolve(testCase.getOutput()).toString());
        }
	}

	public void setPoints(double points) {
		this.points = points;
	}
	
	public double getPoints() {
		return points;
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
	
	public void setMemory(int memory) {
		this.memory = memory;
	}
	
	public int getMemory() {
		return memory;
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
	
	public List<Integer> dependsOn(int groupNumber) {
		if (dependencies.isEmpty()) return new LinkedList<>();
		
		String group = dependencies.split(",")[groupNumber-1].trim();
		if (group.isEmpty()) return new LinkedList<>();
		return Arrays.stream(group.split(";")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
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
	
	public boolean testsScoring() {
		return scoring.equalsIgnoreCase("tests") || scoring.equalsIgnoreCase("icpc");
	}

	public boolean groupsScoring() {
		return !testsScoring();
	}

	public boolean icpcScoring() {
		return scoring.equalsIgnoreCase("icpc"); 
	}
	
	public boolean sumScoring() {
		return scoring.equalsIgnoreCase("sum"); 
	}
	
	public boolean minScoring() {
		return scoring.equalsIgnoreCase("min") || scoring.equalsIgnoreCase("min_fast");
	}
	
	public boolean stopScoringOnFailure() {
		return scoring.equalsIgnoreCase("min_fast"); 
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
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
	
	public boolean hasFilesToDownload() {
		return contestantZip != null;
	}
	
	public boolean isPartial() {
		return precision != -1;
	}
	
	public Set<String> getAllowedExtensions() {
		return allowedExtensions;
	}
	
	public Set<String> getBlacklistedWords() {
		return blacklistedWords;
	}
	
	public boolean isFullFeedback() {
		return feedback.trim().equalsIgnoreCase("full");
	}
	
	public boolean hasSampleTests() {
		return !sample.isEmpty();
	}
	
	public double getPublicScore() {
		if (isFullFeedback()) return Precision.round(getPoints(), getPrecision());
		
		TreeSet<Integer> feedback = feedback();
		double publicWeight = 0.0;
		for (int i = 0; i < getTestGroups().size(); i++) {
			if (feedback.contains(i+1)) {
				publicWeight += getTestGroups().get(i).getWeight();
			}
		}
		return Precision.round(getPoints()*publicWeight, getPrecision());
	}
	
	public TreeSet<Integer> feedback() {
		TreeSet<Integer> set = new TreeSet<>();
		if (isFullFeedback()) return set;
		
		String[] split = getFeedback().split(",");
		for (String s: split) set.add(Integer.valueOf(s.trim()));
		return set;
	}
	
	public TreeSet<Integer> sampleTests() {
		TreeSet<Integer> set = new TreeSet<>();
		if (getSample().trim().isEmpty()) return set;
		
		String[] split = getSample().split(",");
		for (String s: split) set.add(Integer.valueOf(s.trim()));
		return set;
	}
	
	public double getTotalWeight() {
		return getTestGroups().stream().mapToDouble(g -> g.getWeight()).sum();
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
	
	public String getError() {
		return error;
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

	public static void main(String[] args) throws Exception {
		File file = new File("C:\\Users\\pppep\\OneDrive\\Documents\\workspace\\sts\\workdir\\sti15112021\\problems\\16\\task");
		TaskDetails details = new TaskDetails("excel", file);
		System.out.println(details.getContestantZip());
	}
	
	
}