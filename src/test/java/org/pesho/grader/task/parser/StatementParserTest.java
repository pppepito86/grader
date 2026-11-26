package org.pesho.grader.task.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class StatementParserTest {

	public static final String PEAKS_TABLE = String.join("\n",
		"\\subsection{Подзадачи}",
		"\\begin{table}[H]",
		"\t\\begin{tblr}{|Q[c,m]|Q[c,m]|Q[c,m]|Q[c,m]|Q[c,m]|}",
		"\t\t\\hline",
		"\t\t\\textbf{Подзадача} & \\textbf{Точки} & $N$ & $K$ & \\textbf{Други ограничения}\\\\",
		"\t\t\\hline",
		"\t\t$1$ & $0$ & $-$ & $-$ & Примерният тест. \\\\ ",
		"\t\t\\hline",
		"\t\t$2$ & $9$ & $\\leq 20$ & $\\leq 6$ & $-$ \\\\ ",
		"\t\t\\hline",
		"\t\t$3$ & $12$ & $\\leq 70$ & $\\leq 70$ & $-$ \\\\ ",
		"\t\t\\hline",
		"\t\t$4$ & $23$ & $\\leq 300$ & $\\leq 300$ & $h_i = i$ \\\\ ",
		"\t\t\\hline",
		"\t\t$5$ & $16$ & $\\leq 300$ & $\\leq 300$ & $-$ \\\\",
		"\t\t\\hline ",
		"\t\t$6$ & $40$ & $\\leq 2000$ & $\\leq 2000$ & $-$ \\\\",
		"\t\t\\hline",
		"\t\\end{tblr}",
		"\t\\caption*{Точките за дадена подзадача се получават само ако се преминат успешно всички тестове, предвидени за нея.}",
		"\\end{table}");

	public static final String PEAKS_EXAMPLE = String.join("\n",
		"\\subsection{Пример}",
		"\\begin{table}[H]",
		"\t\\begin{tblr}{|l|l|X[j]|}",
		"\t\t\\hline",
		"\t\t\\textbf{Вход} & \\textbf{Изход} & \\textbf{Обяснение на примера} \\\\",
		"\t\t\\hline",
		"\t\t\\texttt{\\makecell[lt]{7 8 \\\\",
		"\t\t4 2 3 1 5 6 7 \\\\",
		"\t\t3 1 2 4 \\\\",
		"\t\t1 2 1 3 \\\\",
		"\t\t4 4 1 7 \\\\",
		"\t\t6 10 1 7 \\\\",
		"\t\t6 20 6 6 \\\\",
		"\t\t6 30 5 5 \\\\",
		"\t\t7 40 1 6 \\\\",
		"\t\t7 50 7 7}}",
		"\t\t& ",
		"\t\t\\texttt{\\makecell[lt]{7 \\\\",
		"\t\t-1 \\\\",
		"\t\t4 \\\\",
		"\t\t10 \\\\",
		"\t\t30 \\\\",
		"\t\t-1 \\\\",
		"\t\t-1 \\\\",
		"\t\t-1}}",
		"\t\t& ",
		"\t\t{Ако започнем със закупуване на фенер $1$ на връх $3$, то можем да извършим следната последователност от действия:\\\\",
		"\t\t\\indent\\tabitem да върви наляво до връх $1$;\\\\",
		"\t\tНе можем да започнем с купуването на фенер $2, 6$ или $7$.} \\\\",
		"\t\t\\hline",
		"\t\\end{tblr}",
		"\\end{table}");

	public static final String PEAKS_SAMPLE_INPUT = "7 8\n4 2 3 1 5 6 7\n3 1 2 4\n1 2 1 3\n4 4 1 7\n6 10 1 7\n6 20 6 6\n6 30 5 5\n7 40 1 6\n7 50 7 7\n";
	public static final String PEAKS_SAMPLE_OUTPUT = "7\n-1\n4\n10\n30\n-1\n-1\n-1\n";

	static final String EXAMPLE_TABLE = String.join("\n",
		"\\begin{tblr}{|l|l|X[j]|}",
		"\\hline",
		"\\textbf{Вход} & \\textbf{Изход} & \\textbf{Обяснение на примера} \\\\",
		"\\hline",
		"\\texttt{\\makecell[lt]{7 8 \\\\ 4 2 3}} & \\texttt{\\makecell[lt]{7 \\\\ -1}} & Обяснение. \\\\",
		"\\hline",
		"\\end{tblr}");

	@Test
	public void limitsFromProblemCommand() {
		String text = "\\problem{Задача A?. ЦВЕТЯ}{$0{,}1$ сек.}{$256$ MB}";
		assertEquals(Arrays.asList("$0{,}1$ сек."), StatementParser.findTimeLimit(text));
		assertEquals(Arrays.asList("$256$ MB"), StatementParser.findMemoryLimit(text));
	}

	@Test
	public void limitsFromProblemCommandWithMacroInTitleAndSpaces() {
		String text = "\\renewcommand{\\problemName}{?. PEAKS}\n\\problem {Задача \\problemName}\n  {$3$ сек.}\n  {$256$ MB}";
		assertEquals(Arrays.asList("$3$ сек."), StatementParser.findTimeLimit(text));
		assertEquals(Arrays.asList("$256$ MB"), StatementParser.findMemoryLimit(text));
	}

	@Test
	public void problemCommandWithoutLimits() {
		assertTrue(StatementParser.findTimeLimit("\\problem{A}{output-only}").isEmpty());
		assertTrue(StatementParser.findMemoryLimit("\\problem{A}{output-only}").isEmpty());
	}

	@Test
	public void limitsFromOldModel() {
		String text = "\\newcommand{\\tl}{0,5}\n\\renewcommand{\\ml}{256}";
		assertEquals(Arrays.asList("0,5"), StatementParser.findTimeLimit(text));
		assertEquals(Arrays.asList("256"), StatementParser.findMemoryLimit(text));
	}

	@Test
	public void oldModelIsUsedAfterProblemCommand() {
		String text = "\\newcommand{\\tl}{0.3}\\newcommand{\\ml}{128}\\problem{A}{\\tl\\ сек.}{\\ml\\ MB}";
		assertEquals(Arrays.asList("\\tl\\ сек.", "0.3"), StatementParser.findTimeLimit(text));
		assertEquals(Arrays.asList("\\ml\\ MB", "128"), StatementParser.findMemoryLimit(text));
	}

	@Test
	public void commentsAreIgnored() {
		assertEquals(Arrays.asList("1"), StatementParser.findTimeLimit("% \\newcommand{\\tl}{9}\n\\newcommand{\\tl}{1}"));
		assertEquals(Arrays.asList("256 \\% x"), StatementParser.findMemoryLimit("\\newcommand{\\ml}{256 \\% x}"));
	}

	@Test
	public void emptyAndUnterminatedArguments() {
		assertEquals(Arrays.asList(""), StatementParser.findTimeLimit("\\newcommand{\\tl}{}"));
		assertTrue(StatementParser.findTimeLimit("\\problem{A}{$1$ s}{$32$ MB").isEmpty());
	}

	@Test
	public void parseNumber() {
		assertEquals(0.1, StatementParser.parseNumber("$0{,}1$ сек."), 0);
		assertEquals(2.5, StatementParser.parseNumber("$2.5$ s"), 0);
		assertEquals(0.5, StatementParser.parseNumber(".5 s"), 0);
		assertEquals(256, StatementParser.parseNumber("$256$ MB ($2^{28}$ B)"), 0);
		assertNull(StatementParser.parseNumber("$?$ сек."));
		assertNull(StatementParser.parseNumber(""));
		assertNull(StatementParser.parseNumber(null));
	}

	@Test
	public void parseNumbers() {
		assertEquals(Arrays.asList(0, 1, 2, 3, 5), StatementParser.parseNumbers("0-3,5"));
		assertEquals(Arrays.asList(0, 1, 2, 3, 5), StatementParser.parseNumbers("0-3;5"));
		assertEquals(Arrays.asList(0, 1, 2), StatementParser.parseNumbers("0 -- 2"));
		assertEquals(Collections.emptyList(), StatementParser.parseNumbers("-"));
		assertEquals(Collections.emptyList(), StatementParser.parseNumbers(""));
	}

	@Test
	public void subtasksTable() {
		List<StatementParser.Subtask> subtasks = StatementParser.findSubtasks(EXAMPLE_TABLE + "\n" + PEAKS_TABLE);
		assertEquals(6, subtasks.size());
		double[] points = {0, 9, 12, 23, 16, 40};
		for (int i = 0; i < subtasks.size(); i++) {
			assertEquals(i + 1, subtasks.get(i).number, 0);
			assertEquals(points[i], subtasks.get(i).points, 0);
			assertNull(subtasks.get(i).dependencies);
		}
	}

	@Test
	public void subtasksTableWithNumberSignAndDependencies() {
		String text = String.join("\n",
			"\\begin{tabularx}{\\textwidth}{|c|c|X|c|}",
			"\\toprule",
			"\\textbf{№} & \\textbf{Точки} & Ограничения & \\textbf{\\makecell{Необходими\\\\подзадачи}} \\\\[2pt]",
			"\\midrule",
			"$0$ & $0$ & примерен & $-$ \\\\ \\cline{1-4}",
			"$1$ & $30$ & $N \\leq 10$ & $0$ \\\\ \\cline{1-4}",
			"$2$ & $70$ & $-$ & $0-1;$ \\\\",
			"\\bottomrule",
			"\\end{tabularx}");
		List<StatementParser.Subtask> subtasks = StatementParser.findSubtasks(text);
		assertEquals(3, subtasks.size());
		assertEquals(0, subtasks.get(0).number, 0);
		assertEquals(Collections.emptyList(), subtasks.get(0).dependencies);
		assertEquals(30, subtasks.get(1).points, 0);
		assertEquals(Arrays.asList(0), subtasks.get(1).dependencies);
		assertEquals(Arrays.asList(0, 1), subtasks.get(2).dependencies);
	}

	@Test
	public void noSubtasksTable() {
		assertNull(StatementParser.findSubtasks(EXAMPLE_TABLE));
		assertNull(StatementParser.findSubtasks("\\section{A} text"));
	}

	@Test
	public void normalizeKeepsNewLinesUpToWhiteSpace() {
		assertEquals("7 8\n4 2", StatementParser.normalize("7 8\n4 2"));
		assertEquals("7 8\n4 2", StatementParser.normalize("  7 \t 8 \r\n4  2\r\n"));
		assertEquals("7 8\n4 2\n", StatementParser.normalize("7 8\n4 2\n\n"));
		assertEquals("7 8\n\n4 2", StatementParser.normalize("7 8\n\n4 2\n"));
		assertEquals("", StatementParser.normalize("\n"));
	}

	@Test
	public void toPlainText() {
		assertEquals("7 8\n4 2 3 1 5 6 7\n3 1 2 4", StatementParser.toPlainText("\\texttt{\\makecell[lt]{7 8 \\\\\n\t\t4 2 3 1 5 6 7 \\\\\n\t\t3 1 2 4}}"));
		assertEquals("7\n-1", StatementParser.toPlainText("\\texttt{\\makecell[lt]{7 \\\\ -1 \\\\ }}"));
		assertEquals("a_b {x} \\n $x", StatementParser.toPlainText("\\texttt{a\\_b \\{x\\} \\textbackslash n \\verb|$x|}"));
		assertEquals("5", StatementParser.toPlainText("\n\\cline{1-3}\n\\hline \\texttt{5} "));
		assertEquals("1 2", StatementParser.toPlainText("\\texttt{1~2}"));
		assertEquals("3 3\n1 5 2\n+ 2\n+ 3\n? 1 5", StatementParser.toPlainText("\\texttt{3\\space3\\\\1\\space5\\space2\\\\+\\space2\\\\+\\space3\\\\?\\space1\\space5}"));
		assertEquals("1 2 3 4 56", StatementParser.toPlainText("\\texttt{1\\,2\\;3\\:4\\ 5\\!6}"));
	}

	@Test
	public void examplesTable() {
		List<StatementParser.Example> examples = StatementParser.findExamples(PEAKS_TABLE + "\n" + PEAKS_EXAMPLE);
		assertEquals(1, examples.size());
		assertEquals(StatementParser.normalize(PEAKS_SAMPLE_INPUT), examples.get(0).input);
		assertEquals(StatementParser.normalize(PEAKS_SAMPLE_OUTPUT), examples.get(0).output);

		examples = StatementParser.findExamples(EXAMPLE_TABLE);
		assertEquals("7 8\n4 2 3", examples.get(0).input);
		assertEquals("7\n-1", examples.get(0).output);
	}

	@Test
	public void examplesFromSeveralTables() {
		List<StatementParser.Example> examples = StatementParser.findExamples(PEAKS_EXAMPLE + "\n" + PEAKS_TABLE + "\n" + EXAMPLE_TABLE);
		assertEquals(2, examples.size());
		assertEquals(StatementParser.normalize(PEAKS_SAMPLE_INPUT), examples.get(0).input);
		assertEquals("7 8\n4 2 3", examples.get(1).input);
		assertEquals("7\n-1", examples.get(1).output);
	}

	@Test
	public void examplesTableWithoutOutput() {
		List<StatementParser.Example> examples = StatementParser.findExamples("\\begin{tabular}{|l|}\n\\hline\n\\textbf{Вход} \\\\\n\\hline\n\\texttt{1 2} \\\\\n\\hline\n\\end{tabular}");
		assertEquals(1, examples.size());
		assertEquals("1 2", examples.get(0).input);
		assertNull(examples.get(0).output);
	}

	@Test
	public void noExamplesTable() {
		assertNull(StatementParser.findExamples(PEAKS_TABLE));
		assertNull(StatementParser.findExamples(PEAKS_TABLE.replace("Други ограничения", "Ограничения на входа")));
		assertNull(StatementParser.findExamples("\\begin{tblr}{|c|c|}\n\\textbf{Функция на участника} & \\textbf{Програма на журито} \\\\\n& \\texttt{play(6)} \\\\\n\\end{tblr}"));
	}
}
