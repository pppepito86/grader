package org.pesho.grader;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;
import org.pesho.grader.task.TaskDetails;

public class ScoreParserTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private TaskDetails task (String properties, int tests) throws Exception {
		Path task = folder.newFolder().toPath();
		Files.createDirectories(task.resolve("tests"));
		Files.write(task.resolve("grade.properties"), properties.getBytes(StandardCharsets.UTF_8));
		for (int i = 1; i <= tests; i++) {
			Files.write(task.resolve(String.format("tests/test.%02d.in", i)), "1\n".getBytes(StandardCharsets.UTF_8));
			Files.write(task.resolve(String.format("tests/test.%02d.sol", i)), "1\n".getBytes(StandardCharsets.UTF_8));
		}
		return new TaskDetails("task", task);
	}

	private static SubmissionScore score () {
		SubmissionScore score = new SubmissionScore("submission");
		score.setCompileResult(new StepResult(Verdict.OK));
		return score;
	}

	private static StepResult result (Verdict verdict, double points, Double checkerOutput) {
		return new StepResult(verdict, "", 0.1, 1000L, points, checkerOutput);
	}

	@Test
	public void zeroPointTestsKeepTheirCheckerOutput() throws Exception {
		TaskDetails details = task("sample=1\n", 4);
		SubmissionScore score = score();
		score.addTestResult(1, result(Verdict.PARTIAL, 0, 0.5));
		score.addTestResult(2, result(Verdict.PARTIAL, 25, 0.75));
		score.addTestResult(3, result(Verdict.OK, 33.333333, 1.0));
		score.addTestResult(4, result(Verdict.WA, 0, 0.0));
		assertEquals("[|0.00000005|],|25|,OK,WA", new ScoreParser(score, details).getVerdict());
	}

	@Test
	public void zeroPointTestWithoutCheckerOutput() throws Exception {
		TaskDetails details = task("sample=1\n", 2);
		SubmissionScore score = score();
		score.addTestResult(1, result(Verdict.PARTIAL, 0, null));
		score.addTestResult(2, result(Verdict.OK, 100, 1.0));
		assertEquals("[|0|],OK", new ScoreParser(score, details).getVerdict());
	}

	@Test
	public void zeroPointGroupKeepsItsCheckerOutput() throws Exception {
		TaskDetails details = task("groups=1,2,3\nweights=0,40,60\n", 3);
		SubmissionScore score = score();
		score.addGroupResult(1, result(Verdict.PARTIAL, 0, 0.25));
		score.addGroupResult(2, result(Verdict.PARTIAL, 10, 0.25));
		score.addGroupResult(3, result(Verdict.OK, 60, 1.0));
		assertEquals("(0.000000025),(10),60", new ScoreParser(score, details).getVerdict());
	}
}
