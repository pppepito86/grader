package org.pesho.grader.task;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pesho.grader.task.parser.StatementParserTest;

public class TaskDetailsStatementTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private static final String PROPERTIES = "time=3\nmemory=256\npoints=100\n";
	private static final String PEAKS = PROPERTIES + "groups=1,2,3,4,5,6\nweights=0,9,12,23,16,40\n";
	private static final String DEPENDENCIES = PROPERTIES + "groups=1,2,3,4,5,6\nweights=0,20,20,20,20,20\ndependencies=,0,1,0-2,,0-3;4\n";

	private static String dependenciesTable (String... rows) {
		StringBuilder table = new StringBuilder("\\begin{tabularx}{\\textwidth}{|c|c|X|c|}\n\\toprule\n");
		table.append("\\textbf{№} & \\textbf{Точки} & Ограничения & \\textbf{\\makecell{Необходими\\\\подзадачи}} \\\\[2pt]\n\\midrule\n");
		for (String row : rows) table.append(row).append(" \\\\\n\\cline{1-4}\n");
		return table.append("\\bottomrule\n\\end{tabularx}").toString();
	}

	private static final String[] DEPENDENCIES_ROWS = {
		"$0$ & $0$ & a & $-$",
		"$1$ & $20$ & b & $0$",
		"$2$ & $20$ & c & $1$",
		"$3$ & $20$ & d & $0-2$",
		"$4$ & $20$ & e & ",
		"$5$ & $20$ & f & $0-3;4$"};

	/** whether the next task has a checker and no solution files */
	private boolean checker = false;

	/**
	 * The statement errors of a task with the given properties, number of tests and statement body.
	 * files are the input and the solution of the first tests (the other tests are 1).
	 */
	private TreeSet<String> errors (String properties, int tests, String statement, String... files) throws Exception {
		File dir = folder.newFolder();
		Path task = dir.toPath();
		Files.createDirectories(task.resolve("tests"));
		Files.write(task.resolve("grade.properties"), properties.getBytes(StandardCharsets.UTF_8));
		if (checker) Files.write(task.resolve("checker"), "#!/bin/sh\n".getBytes(StandardCharsets.UTF_8));
		for (int i = 1; i <= tests; i++) {
			String input = 2*i-2 < files.length ? files[2*i-2] : "1\n";
			String solution = 2*i-1 < files.length ? files[2*i-1] : "1\n";
			Files.write(task.resolve(String.format("tests/test.%02d.in", i)), input.getBytes(StandardCharsets.UTF_8));
			if (!checker) Files.write(task.resolve(String.format("tests/test.%02d.sol", i)), solution.getBytes(StandardCharsets.UTF_8));
		}
		String tex = "\\documentclass{article}\n\\begin{document}\n" + statement + "\n\\end{document}\n";
		Files.write(task.resolve("statement.tex"), tex.getBytes(StandardCharsets.UTF_8));

		TreeSet<String> errors = new TreeSet<>();
		for (Map.Entry<String, String> error : new TaskDetails("task", task).getError().entrySet()) {
			if (error.getKey().equals("exception")) errors.add("exception: " + error.getValue());
			if ("backend.different_in_tex_statement".equals(error.getValue())) errors.add(error.getKey());
		}
		return errors;
	}

	private static TreeSet<String> set (String... values) {
		return new TreeSet<>(Arrays.asList(values));
	}

	private static String problem (String body) {
		return "\\problem{Задача A1. X}{$3$ сек.}{$256$ MB}\n" + body;
	}

	@Test
	public void subtasksMatch() throws Exception {
		assertEquals(set(), errors(PEAKS + "sample=1\n", 6, problem(StatementParserTest.PEAKS_TABLE)));
	}

	@Test
	public void subtasksNumberedFromOneForGroupsFromZero() throws Exception {
		// without sample=1 the zero-points first group makes the groups numbered from 0
		assertEquals(set("groups_property"), errors(PEAKS, 6, problem(StatementParserTest.PEAKS_TABLE)));
	}

	@Test
	public void differentTimeAndPoints() throws Exception {
		String properties = PEAKS.replace("time=3", "time=2") + "sample=1\n";
		assertEquals(set("time_property", "weights_property"), errors(properties, 6, problem(StatementParserTest.PEAKS_TABLE.replace("$12$", "$13$"))));
	}

	@Test
	public void differentNumberOfSubtasks() throws Exception {
		String properties = PROPERTIES + "groups=1,2,3,4,5;6\nweights=0,9,12,23,56\nsample=1\n";
		assertEquals(set("groups_property"), errors(properties, 6, problem(StatementParserTest.PEAKS_TABLE)));
	}

	@Test
	public void dependenciesMatch() throws Exception {
		assertEquals(set(), errors(DEPENDENCIES, 6, problem(dependenciesTable(DEPENDENCIES_ROWS))));
	}

	@Test
	public void dependenciesWithComma() throws Exception {
		String[] rows = DEPENDENCIES_ROWS.clone();
		rows[5] = "$5$ & $20$ & f & $0-3, 4$";
		assertEquals(set(), errors(DEPENDENCIES, 6, problem(dependenciesTable(rows))));
	}

	@Test
	public void differentDependencies() throws Exception {
		String[] rows = DEPENDENCIES_ROWS.clone();
		rows[3] = "$3$ & $20$ & d & $0-1$";
		assertEquals(set("dependencies_property"), errors(DEPENDENCIES, 6, problem(dependenciesTable(rows))));
	}

	@Test
	public void dependenciesMissingFromTable() throws Exception {
		StringBuilder table = new StringBuilder("\\begin{tblr}{|c|c|}\n\\hline\n\\textbf{№} & \\textbf{Точки} \\\\\n\\hline\n$0$ & $0$ \\\\\n");
		for (int i = 1; i <= 5; i++) table.append("$").append(i).append("$ & $20$ \\\\\n");
		table.append("\\hline\n\\end{tblr}");
		assertEquals(set("dependencies_property"), errors(DEPENDENCIES, 6, problem(table.toString())));
		assertEquals(set(), errors(DEPENDENCIES.replaceAll("dependencies=.*\n", ""), 6, problem(table.toString())));
	}

	@Test
	public void dependenciesMissingFromProperties() throws Exception {
		assertEquals(set("dependencies_property"), errors(DEPENDENCIES.replaceAll("dependencies=.*\n", ""), 6, problem(dependenciesTable(DEPENDENCIES_ROWS))));
	}

	@Test
	public void dependenciesTableNumberedFromOneForGroupsFromZero() throws Exception {
		String[] rows = DEPENDENCIES_ROWS.clone();
		for (int i = 0; i < rows.length; i++) rows[i] = rows[i].replaceFirst("^\\$" + i + "\\$", "\\$" + (i + 1) + "\\$");
		assertEquals(set("groups_property"), errors(DEPENDENCIES, 6, problem(dependenciesTable(rows))));
	}

	@Test
	public void oldModelWithoutTable() throws Exception {
		assertEquals(set(), errors(PROPERTIES + "groups=1,2\nweights=50,50\n", 2, "\\newcommand{\\tl}{3}\n\\newcommand{\\ml}{256}"));
	}

	@Test
	public void missingNumberDoesNotBreakTheTask() throws Exception {
		assertEquals(set("time_property", "memory_property"), errors(PROPERTIES + "groups=1,2\nweights=50,50\n", 2, "\\newcommand{\\tl}{}\n\\newcommand{\\ml}{TBD}"));
	}

	@Test
	public void subtasksTableForTestsScoring() throws Exception {
		assertEquals(set("groups_property"), errors(PROPERTIES, 3, problem(StatementParserTest.PEAKS_TABLE)));
	}

	private static final String PEAKS_STATEMENT = problem(StatementParserTest.PEAKS_TABLE + "\n" + StatementParserTest.PEAKS_EXAMPLE);
	private static final String INPUT = StatementParserTest.PEAKS_SAMPLE_INPUT;
	private static final String OUTPUT = StatementParserTest.PEAKS_SAMPLE_OUTPUT;

	private static String examplesTable (boolean withOutput, String... cells) {
		StringBuilder table = new StringBuilder("\\begin{tblr}{|l|l|}\n\\hline\n\\textbf{Вход}" + (withOutput ? " & \\textbf{Изход}" : "") + " \\\\\n\\hline\n");
		for (int i = 0; i < cells.length; i += withOutput ? 2 : 1) {
			table.append("\\texttt{\\makecell[lt]{").append(cells[i].replace("\n", " \\\\\n")).append("}}");
			if (withOutput) table.append(" & \\texttt{\\makecell[lt]{").append(cells[i + 1].replace("\n", " \\\\\n")).append("}}");
			table.append(" \\\\\n\\hline\n");
		}
		return table.append("\\end{tblr}").toString();
	}

	@Test
	public void sampleSubtaskMatchesExamples() throws Exception {
		assertEquals(set(), errors(PEAKS + "sample=1\n", 6, PEAKS_STATEMENT, INPUT, OUTPUT));
	}

	@Test
	public void examplesMatchUpToWhiteSpace() throws Exception {
		String input = "  " + INPUT.replace(" ", "  \t").replace("\n", " \r\n");
		assertEquals(set(), errors(PEAKS + "sample=1\n", 6, PEAKS_STATEMENT, input, OUTPUT.substring(0, OUTPUT.length() - 1)));
	}

	@Test
	public void differentExampleOutput() throws Exception {
		assertEquals(set("sample_property"), errors(PEAKS + "sample=1\n", 6, PEAKS_STATEMENT, INPUT, OUTPUT.replace("30", "31")));
	}

	@Test
	public void differentExampleNewLines() throws Exception {
		assertEquals(set("sample_property"), errors(PEAKS + "sample=1\n", 6, PEAKS_STATEMENT, INPUT.replaceFirst("\n", " "), OUTPUT));
	}

	@Test
	public void extraEmptyLineInSample() throws Exception {
		assertEquals(set("sample_property"), errors(PEAKS + "sample=1\n", 6, PEAKS_STATEMENT, INPUT + "\n", OUTPUT));
	}

	@Test
	public void sampleTestsMatchExamplesInOrder() throws Exception {
		String statement = problem(examplesTable(true, "1 2", "3", "5 5", "10"));
		assertEquals(set(), errors(PROPERTIES + "sample=1;2\n", 3, statement, "1 2\n", "3\n", "5 5\n", "10\n"));
	}

	@Test
	public void sampleTestsInDifferentOrder() throws Exception {
		String statement = problem(examplesTable(true, "5 5", "10", "1 2", "3"));
		assertEquals(set("sample_property"), errors(PROPERTIES + "sample=1;2\n", 3, statement, "1 2\n", "3\n", "5 5\n", "10\n"));
	}

	@Test
	public void examplesInSeveralTables() throws Exception {
		String first = examplesTable(true, "1 2", "3"), second = examplesTable(true, "5 5", "10");
		String[] files = {"1 2\n", "3\n", "5 5\n", "10\n"};
		assertEquals(set(), errors(PROPERTIES + "sample=1;2\n", 3, problem(first + "\n" + second), files));
		assertEquals(set("sample_property"), errors(PROPERTIES + "sample=1;2\n", 3, problem(second + "\n" + first), files));
		assertEquals(set("sample_property"), errors(PROPERTIES + "sample=1;2\n", 3, problem(first), files));
	}

	@Test
	public void differentNumberOfExamples() throws Exception {
		String statement = problem(examplesTable(true, "1 2", "3"));
		assertEquals(set("sample_property"), errors(PROPERTIES + "sample=1;2\n", 3, statement, "1 2\n", "3\n", "5 5\n", "10\n"));
	}

	@Test
	public void noSampleTests() throws Exception {
		assertEquals(set(), errors(PROPERTIES, 3, problem(examplesTable(true, "7", "8"))));
	}

	@Test
	public void examplesTableWithoutOutput() throws Exception {
		String statement = problem(examplesTable(false, "1 2", "5 5"));
		assertEquals(set("sample_property"), errors(PROPERTIES + "sample=1;2\n", 3, statement, "1 2\n", "3\n", "5 5\n", "10\n"));
		checker = true;
		assertEquals(set(), errors(PROPERTIES + "sample=1;2\n", 3, statement, "1 2\n", null, "5 5\n", null));
		assertEquals(set("sample_property"), errors(PROPERTIES + "sample=1;2\n", 3, statement, "1 3\n", null, "5 5\n", null));
	}

	@Test
	public void zeroPointsFirstGroupIsTheSampleSubtask() throws Exception {
		String statement = problem(StatementParserTest.PEAKS_EXAMPLE);
		assertEquals(set(), errors(PEAKS, 6, statement, INPUT, OUTPUT));
		assertEquals(set("sample_property"), errors(PEAKS, 6, statement, INPUT.replace("7 8", "7 9"), OUTPUT));
	}

	private static final String TRIP = String.join("\n",
		"\\renewcommand{\\tl}{$1.6$ сек.}",
		"\\renewcommand{\\ml}{$1024$ MB}",
		"\\problem{Задача А3. ПЪТЕШЕСТВИЕ}",
		"\\subsection{Подзадачи}",
		"\\begin{table}[H]",
		"\\begin{tblr}{|Q[c,m]|Q[c,m]|Q[c,m]|Q[c,m]|X[c,m]|}",
		"\\hline",
		"\\textbf{№} & \\textbf{Точки} & \\textbf{\\makecell{Необходими\\\\подзадачи}} & $N$ & \\textbf{Други ограничения}\\\\",
		"\\hline",
		"$0$ & $0$ & $-$ & $-$ & Примерните тестове. \\\\",
		"\\hline",
		"$1$ & $9$ & $-$ & \\SetCell[r=2]{}{$\\le 5 \\times 10^3$} & Няма платени полети: $C_i \\leq 0$. \\\\",
		"\\hline",
		"$2$ & $10$ & $-$ & & Няма субсидирани полети: $C_i \\geq 0$. \\\\",
		"\\hline",
		"$3$ & $16$ & $-$ & $\\le 500$ & Ако отговорът е да, има начин балансът да достигне поне $2 \\times 10^{50}$ кредита. \\\\",
		"\\hline",
		"$4$ & $8$ & $3$ & \\SetCell[r=2]{}{$\\le 5 \\times 10^3$} & Ако отговорът е да, има начин балансът да достигне поне $2 \\times 10^{50}$ кредита \\\\",
		"\\hline",
		"$5$ & $24$ & $-$ & & Няма начин балансът да достигне повече от $2 \\times 10^{50}$ кредита. \\\\",
		"\\hline",
		"$6$ & $25$ & $3$ & $\\le 500$ & -- \\\\",
		"\\hline",
		"$7$ & $8$ & $0-5$ & $\\le 5 \\times 10^3$ & -- \\\\",
		"\\hline",
		"\\end{tblr}",
		"\\caption*{Точките за дадена подзадача се получават само ако се преминат успешно всички тестове, предвидени за нея и необходимите подзадачи.}",
		"\\end{table}",
		"\\FloatBarrier",
		"\\subsection{Примери}",
		"\\begin{table}[H]",
		"\\begin{tblr}{width=0.6\\textwidth, colspec={|l|l|X[j]|}}",
		"\\hline",
		"\\textbf{Вход} & \\textbf{Изход} & \\textbf{Илюстрация на примера} \\\\",
		"\\hline",
		"\\texttt{\\makecell[lt]{6 6\\\\",
		"0 1 -3\\\\", "0 2 -5\\\\", "0 4 0\\\\", "1 2 -4\\\\", "2 3 2\\\\", "3 0 4\\\\",
		"5 2 2}} &",
		"\\texttt{1} &",
		"{\\begin{minipage}[t]{\\linewidth}",
		"\t\\vspace{-0.2cm}",
		"\t\\begin{adjustbox}{width=\\textwidth, valign=t, center}",
		"\t\t\\includesvg[inkscapelatex=false]{example1}",
		"\t\\end{adjustbox}",
		"\\end{minipage}} \\\\",
		"\\hline",
		"\\texttt{\\makecell[lt]{6 7\\\\",
		"0 1 -1\\\\", "0 2 -2\\\\", "0 4 1\\\\", "1 2 3\\\\", "2 3 -3\\\\", "3 0 6\\\\",
		"5 2 -1}} &",
		"\\texttt{0} &",
		"{\\begin{minipage}[t]{\\linewidth}",
		"\t\\includesvg[inkscapelatex=false]{example2}",
		"\\end{minipage}} \\\\",
		"\\hline",
		"\\end{tblr}",
		"\\end{table}");
	private static final String TRIP_PROPERTIES = "time=1.6\nmemory=1024\ngroups=1-2,3,4,5,6,7,8,9\nweights=0,9,10,16,8,24,25,8\ndependencies=,,,,3,,3,0-6\n";
	private static final String[] TRIP_SAMPLES = {"6 7\n0 1 -3\n0 2 -5\n0 4 0\n1 2 -4\n2 3 2\n3 0 4\n5 2 2\n", "1\n", "6 7\n0 1 -1\n0 2 -2\n0 4 1\n1 2 3\n2 3 -3\n3 0 6\n5 2 -1\n", "0\n"};

	@Test
	public void tripMistakesAreReported() throws Exception {
		assertEquals(set("dependencies_property", "sample_property"), errors(TRIP_PROPERTIES, 9, TRIP, TRIP_SAMPLES));
		String fixed = TRIP.replace("$0-5$", "$0-6$").replace("{6 6\\\\", "{6 7\\\\");
		assertEquals(set(), errors(TRIP_PROPERTIES, 9, fixed, TRIP_SAMPLES));
	}

	@Test
	public void samplesWithoutSolutionFiles() throws Exception {
		checker = true;
		String statement = problem(examplesTable(true, "1 2", "any correct answer"));
		assertEquals(set(), errors(PROPERTIES + "sample=1\n", 3, statement, "1 2\n", null));
		assertEquals(set("sample_property"), errors(PROPERTIES + "sample=1\n", 3, statement, "1 3\n", null));
	}
}
