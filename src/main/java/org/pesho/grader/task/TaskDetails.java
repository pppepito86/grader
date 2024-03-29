package org.pesho.grader.task;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;

public class TaskDetails {

	private double points;
	private int precision;
	private double time;
	private double ioTime;
	private int memory;
	private int rejudgeTimes;
	private String checker;
	private String graderDir;
	private String feedback;
	private String groups;
	private String weights;
	private String scoring;
	private List<TestGroup> testGroups;
	private String description;
	private String contestantZip;
	private String extensions;
	private Set<String> allowedExtensions;
	private boolean isInteractive;
	
	public static final TaskDetails EMPTY = new TaskDetails(new Properties(), null);
	
	public static TaskDetails create(TaskParser taskParser) {
		return new TaskDetails(taskParser);
	}
	
	public TaskDetails() {
	}
	
	public TaskDetails(Properties props, String checker, TestGroup... testGroups) {
        this.points = Double.valueOf(props.getProperty("points", "100"));
		this.precision = Integer.valueOf(props.getProperty("precision", "-1"));
        this.time = Double.valueOf(props.getProperty("time", "1"));
        this.ioTime = Double.valueOf(props.getProperty("io_time", "0"));
        this.memory = Integer.valueOf(props.getProperty("memory", "256"));
		this.rejudgeTimes = Integer.valueOf(props.getProperty("rejudge", "1"));
        this.feedback = props.getProperty("feedback", "FULL").trim();
        this.groups = props.getProperty("groups", "").trim();
        this.weights = props.getProperty("weights", "").trim();
        this.scoring = props.getProperty("scoring", this.groups.isEmpty()?"tests":"min_fast").trim();
        this.extensions = props.getProperty("extensions", "cpp").trim();
		this.allowedExtensions = Arrays.stream(extensions.split(",")).map(s -> s.trim()).collect(Collectors.toSet());
        this.checker = checker;
        this.testGroups = new ArrayList<>();
	}
	
	public TaskDetails(TaskParser taskParser) {
		Properties props = new Properties();
		if (taskParser.getProperties().exists()) {
			try (FileInputStream fileInputStream = new FileInputStream(taskParser.getProperties())) {
				props.load(fileInputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.points = Double.valueOf(props.getProperty("points", "100.0"));
		this.precision = Integer.valueOf(props.getProperty("precision", "-1"));
		this.time = Double.valueOf(props.getProperty("time", "1"));
        this.ioTime = Double.valueOf(props.getProperty("io_time", "0"));
		this.memory = Integer.valueOf(props.getProperty("memory", "256"));
		this.rejudgeTimes = Integer.valueOf(props.getProperty("rejudge", "1"));
		this.feedback = props.getProperty("feedback", "FULL").trim();
		this.groups = props.getProperty("groups", "").trim();
        this.weights = props.getProperty("weights", "").trim();
        this.scoring = props.getProperty("scoring", this.groups.isEmpty()?"tests":"min_fast").trim();
		this.checker = taskParser.getChecker().getAbsolutePath();
		this.graderDir = taskParser.getGraderDir().getAbsolutePath();
        this.extensions = props.getProperty("extensions", "cpp").trim();
        this.isInteractive = taskParser.getGraderDir().exists();
		
		this.description = taskParser.getDescription().map(f -> f.getAbsolutePath()).orElse(null);
		this.contestantZip = taskParser.getContestantZip().map(f -> f.getAbsolutePath()).orElse(null);
		
		this.allowedExtensions = Arrays.stream(extensions.split(",")).map(s -> s.trim()).collect(Collectors.toSet());
		
		TestCase[] testCases = new TestCase[taskParser.testsCount()];
		for (int i = 0; i < testCases.length; i++) {
			testCases[i] = new TestCase(i+1, taskParser.getInput().get(i).getAbsolutePath(), taskParser.getOutput().get(i).getAbsolutePath());
		}
		
		Set<Integer> feedbackGroups = feedback();
		TestGroup[] testGroups = null;
		if (groups.isEmpty()) {
			testGroups = new TestGroup[testCases.length];
			for (int i = 0; i < testGroups.length; i++) {
				boolean hasFeedback = isFullFeedback() || feedbackGroups.contains(i+1);
				testGroups[i] = new TestGroup(1.0/testCases.length, hasFeedback, testCases[i]);
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
					cases[j-first] = testCases[j-1];
				}
				
				double weight = (weightsSplit.length == groupsSplit.length) ? Double.valueOf(weightsSplit[i].trim()) : 1;
				boolean hasFeedback = isFullFeedback() || feedbackGroups.contains(i+1);
				testGroups[i] = new TestGroup(weight/totalWeight, hasFeedback, cases);
			}
		}
		
		this.testGroups = Arrays.asList(testGroups);
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
	
	public void setChecker(String checker) {
		this.checker = checker;
	}
	
	public String getChecker() {
		return checker;
	}

	public String getGraderDir() {
		return graderDir;
	}
	
	public void setTestGroups(List<TestGroup> testGroups) {
		this.testGroups = testGroups;
	}
	
	public List<TestGroup> getTestGroups() {
		return testGroups;
	}
	
	public boolean testsScoring() {
		return scoring.equalsIgnoreCase("tests");
	}

	public boolean groupsScoring() {
		return !testsScoring();
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
	
	public String getContestantZip() {
		return contestantZip;
	}
	
	public void setContestantZip(String contestantZip) {
		this.contestantZip = contestantZip;
	}
	
	public boolean isInteractive() {
		return isInteractive;
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
	
	public boolean isFullFeedback() {
		return feedback.trim().equalsIgnoreCase("full");
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
	
	public double getTotalWeight() {
		return getTestGroups().stream().mapToDouble(g -> g.getWeight()).sum();
	}
	
}