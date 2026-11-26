package org.pesho.grader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.pesho.grader.step.StepResult;
import org.pesho.grader.step.Verdict;

public class SubmissionScoreTest {

	private static SubmissionScore score (StepResult... groups) {
		SubmissionScore score = new SubmissionScore("submission");
		for (int i = 0; i < groups.length; i++) score.addGroupResult(i + 1, groups[i]);
		return score;
	}

	private static StepResult group (Verdict verdict, Double time, Long memory) {
		return new StepResult(verdict, "", time, memory, 0.0);
	}

	@Test
	public void maximumOfPassedGroups() {
		SubmissionScore score = score(group(Verdict.OK, 0.5, 1000L), group(Verdict.WA, 0.7, 3000L), group(Verdict.TL, -1.5, 2000L));
		assertEquals(0.7, score.findTime(), 0);
		assertEquals(3000L, (long) score.findMemory());
	}

	@Test
	public void allGroupsWhenNoneIsPassed() {
		SubmissionScore score = score(group(Verdict.TL, 1.2, 3000L), group(Verdict.RE, 0.3, 5000L), group(Verdict.SKIPPED, null, null));
		assertEquals(1.2, score.findTime(), 0);
		assertEquals(5000L, (long) score.findMemory());
	}

	@Test
	public void allGroupsOverTheLimitWhenNoneIsPassed() {
		SubmissionScore score = score(group(Verdict.TL, 1.2, 1000L), group(Verdict.TL, -1.5, -262144L), group(Verdict.ML, -1.5, -262144L));
		assertEquals(-1.5, score.findTime(), 0);
		assertEquals(-262144L, (long) score.findMemory());
	}

	@Test
	public void noGroupsWithTime() {
		SubmissionScore score = score(group(Verdict.TL, null, null), group(Verdict.SKIPPED, null, null));
		assertNull(score.findTime());
		assertNull(score.findMemory());
	}

	@Test
	public void passedGroupOverTheLimit() {
		SubmissionScore score = score(group(Verdict.OK, 0.5, 1000L), group(Verdict.PARTIAL, -1.5, -262144L));
		assertEquals(-1.5, score.findTime(), 0);
		assertEquals(-262144L, (long) score.findMemory());
	}
}
