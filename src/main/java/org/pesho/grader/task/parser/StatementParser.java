package org.pesho.grader.task.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StatementParser {

	private static final Pattern NUMBER = Pattern.compile("\\d*[.,]\\d+|\\d+");
	private static final Pattern NUMBERS = Pattern.compile("(\\d{1,9})\\s*(?:-+|–|—)\\s*(\\d{1,9})|(\\d{1,9})");
	private static final Pattern TABLE = Pattern.compile("\\\\begin\\{(tblr|longtblr|tabular|tabularx|tabulary|longtable)\\}");

	private static final String[] SUBTASK_COLUMN = {"подзадач", "subtask", "№"};
	private static final String[] POINTS_COLUMN = {"точки", "points"};
	private static final String[] DEPENDENCIES_COLUMN = {"необходим", "required"};
	private static final String[] INPUT_COLUMN = {"вход", "input"};
	private static final String[] OUTPUT_COLUMN = {"изход", "output"};

	private static final Map<String, String> SYMBOLS = new HashMap<String, String>() {{
		put("textbackslash", "\\");
		put("textasciitilde", "~");
		put("textasciicircum", "^");
		put("textunderscore", "_");
		put("textbar", "|");
		put("textless", "<");
		put("textgreater", ">");
		put("newline", "\n");
		put("linebreak", "\n");
		put("space", " ");
		put("nobreakspace", " ");
		put("enspace", " ");
		put("thinspace", " ");
		put("quad", " ");
		put("qquad", " ");
	}};

	public static class Subtask {
		public final Double number;
		public final Double points;
		/** null when the table has no dependencies column */
		public final List<Integer> dependencies;

		Subtask (Double number, Double points, List<Integer> dependencies) {
			this.number = number;
			this.points = points;
			this.dependencies = dependencies;
		}
	}

	public static class Example {
		/** normalized text, see {@link StatementParser#normalize(String)} */
		public final String input;
		/** normalized text, null when the table has no output column */
		public final String output;

		Example (String input, String output) {
			this.input = input;
			this.output = output;
		}
	}

	/**
	 * Values of the time limit in priority order: \problem{title}{time limit}{memory limit},
	 * then the old \newcommand{\tl}{...}. Empty when the statement does not show the limit.
	 */
	public static List<String> findTimeLimit (String text) {
		return findLimit(text, 1, "newcommand{\\tl}");
	}

	/**
	 * Values of the memory limit in priority order: \problem{title}{time limit}{memory limit},
	 * then the old \newcommand{\ml}{...}. Empty when the statement does not show the limit.
	 */
	public static List<String> findMemoryLimit (String text) {
		return findLimit(text, 2, "newcommand{\\ml}");
	}

	private static List<String> findLimit (String text, int problemArgument, String oldCommand) {
		text = removeComments(text);
		List<String> values = new ArrayList<>();
		List<String> problem = findArguments(text, "\\problem", 3);
		if (problem.size() == 3) values.add(problem.get(problemArgument));
		values.addAll(findArguments(text, oldCommand, 1));
		return values;
	}

	/**
	 * Rows of the subtasks table - the table whose header row has a subtask (or №) and a points column
	 * and optionally a dependencies column (e.g. 0-3,5 or 0-3;5). null when there is no such table.
	 */
	public static List<Subtask> findSubtasks (String text) {
		List<List<String>> rows = findTable(text, header -> findColumn(header, SUBTASK_COLUMN) != -1 && findColumn(header, POINTS_COLUMN) != -1);
		if (rows == null) return null;
		List<String> header = cleanRow(rows.get(0));
		int subtaskColumn = findColumn(header, SUBTASK_COLUMN);
		int pointsColumn = findColumn(header, POINTS_COLUMN);
		int dependenciesColumn = findColumn(header, DEPENDENCIES_COLUMN);

		List<Subtask> subtasks = new ArrayList<>();
		for (List<String> row : rows.subList(1, rows.size())) {
			List<String> cells = cleanRow(row);
			if (cells.stream().allMatch(String::isEmpty)) continue;
			subtasks.add(new Subtask(
				parseNumber(cell(cells, subtaskColumn)),
				parseNumber(cell(cells, pointsColumn)),
				dependenciesColumn == -1 ? null : parseNumbers(cell(cells, dependenciesColumn))));
		}
		return subtasks;
	}

	/**
	 * Rows of the examples tables, in their order - the tables whose header row has an input column, optionally an
	 * output column and no points column (that is the subtasks table), as normalized plain text. null when there is no such table.
	 */
	public static List<Example> findExamples (String text) {
		List<List<List<String>>> tables = findTables(text, header -> findColumn(header, INPUT_COLUMN) != -1 && findColumn(header, POINTS_COLUMN) == -1);
		if (tables.isEmpty()) return null;

		List<Example> examples = new ArrayList<>();
		for (List<List<String>> rows : tables) {
			List<String> header = cleanRow(rows.get(0));
			int inputColumn = findColumn(header, INPUT_COLUMN);
			int outputColumn = findColumn(header, OUTPUT_COLUMN);
			for (List<String> row : rows.subList(1, rows.size())) {
				if (cleanRow(row).stream().allMatch(String::isEmpty)) continue;
				examples.add(new Example(toPlainText(cell(row, inputColumn)), outputColumn == -1 ? null : toPlainText(cell(row, outputColumn))));
			}
		}
		return examples;
	}

	public static String removeComments (String text) {
		return text.replaceAll("(?m)(?<!\\\\)%.*$", "");
	}

	/** Up to count brace arguments of the first occurrence of command. */
	public static List<String> findArguments (String text, String command, int count) {
		List<String> arguments = new ArrayList<>();
		int start = text.indexOf(command);
		while (start != -1) {
			int i = start + command.length();
			if (i < text.length() && (text.charAt(i) == '{' || Character.isWhitespace(text.charAt(i)))) break;
			start = text.indexOf(command, i);
		}
		if (start == -1) return arguments;

		int i = start + command.length();
		while (arguments.size() < count) {
			while (i < text.length() && Character.isWhitespace(text.charAt(i))) i++;
			if (i >= text.length() || text.charAt(i) != '{') break;
			int level = 0, begin = i + 1;
			for (; i < text.length(); i++) {
				char current = text.charAt(i);
				if (current == '\\') i++;
				else if (current == '{') level++;
				else if (current == '}' && --level == 0) break;
			}
			if (i >= text.length()) break;
			arguments.add(text.substring(begin, i++));
		}
		return arguments;
	}

	/** The first of {@link #findTables(String, Predicate)} or null when there is no such table. */
	private static List<List<String>> findTable (String text, Predicate<List<String>> isHeader) {
		List<List<List<String>>> tables = findTables(text, isHeader);
		return tables.isEmpty() ? null : tables.get(0);
	}

	/** Raw cells of the tables with a header row (cleaned cells) accepted by isHeader, in their order, each starting from its header row. */
	private static List<List<List<String>>> findTables (String text, Predicate<List<String>> isHeader) {
		text = removeComments(text);
		List<List<List<String>>> tables = new ArrayList<>();
		Matcher table = TABLE.matcher(text);
		while (table.find()) {
			String environment = table.group(1);
			int end = text.indexOf("\\end{" + environment + "}", table.end());
			if (end == -1) break;
			int start = skipArguments(text, table.end(), environment.equals("tabularx") || environment.equals("tabulary") ? 2 : 1);
			if (start == -1 || start > end) continue;

			List<List<String>> rows = splitTable(text.substring(start, end));
			for (int r = 0; r < rows.size(); r++) {
				if (isHeader.test(cleanRow(rows.get(r)))) {
					tables.add(rows.subList(r, rows.size()));
					break;
				}
			}
		}
		return tables;
	}

	private static int skipArguments (String text, int i, int count) {
		while (i < text.length() && Character.isWhitespace(text.charAt(i))) i++;
		if (i < text.length() && text.charAt(i) == '[') i = text.indexOf(']', i) + 1;
		if (i == 0) return -1;
		for (int k = 0; k < count; k++) {
			while (i < text.length() && Character.isWhitespace(text.charAt(i))) i++;
			if (i >= text.length() || text.charAt(i) != '{') return -1;
			int level = 0;
			for (; i < text.length(); i++) {
				char current = text.charAt(i);
				if (current == '\\') i++;
				else if (current == '{') level++;
				else if (current == '}' && --level == 0) break;
			}
			if (i >= text.length()) return -1;
			i++;
		}
		return i;
	}

	/** Raw cells of the table rows, split by \\ and & outside of braces. */
	private static List<List<String>> splitTable (String body) {
		List<List<String>> rows = new ArrayList<>();
		List<String> row = new ArrayList<>();
		StringBuilder cell = new StringBuilder();
		int level = 0;
		for (int i = 0; i < body.length(); i++) {
			char current = body.charAt(i);
			if (current == '\\' && i + 1 < body.length()) {
				if (level == 0 && body.charAt(i + 1) == '\\') {
					row.add(cell.toString());
					rows.add(row);
					row = new ArrayList<>();
					cell = new StringBuilder();
					i += 2;
					if (i < body.length() && body.charAt(i) == '*') i++;
					while (i < body.length() && Character.isWhitespace(body.charAt(i))) i++;
					if (i < body.length() && body.charAt(i) == '[' && body.indexOf(']', i) != -1) i = body.indexOf(']', i) + 1;
					i--;
					continue;
				}
				cell.append(current).append(body.charAt(++i));
				continue;
			}
			if (current == '{') level++;
			else if (current == '}') level--;
			else if (current == '&' && level == 0) {
				row.add(cell.toString());
				cell = new StringBuilder();
				continue;
			}
			cell.append(current);
		}
		row.add(cell.toString());
		rows.add(row);
		return rows;
	}

	private static String removeTableRules (String cell) {
		return cell
			.replaceAll("\\\\(hline|toprule|midrule|bottomrule)(\\[[^\\]]*\\])?", " ")
			.replaceAll("\\\\(cline|cmidrule)(\\([^)]*\\))?\\{[^}]*\\}", " ")
			.replaceAll("\\\\(SetCell|SetRow|SetCells)(\\[[^\\]]*\\])?\\{[^}]*\\}", " ");
	}

	/** The cells with LaTeX commands, braces and $ removed. */
	private static List<String> cleanRow (List<String> row) {
		return row.stream()
			.map(cell -> removeTableRules(cell)
				.replaceAll("\\\\[a-zA-Z]+\\*?", " ")
				.replaceAll("[{}$]", " ")
				.trim())
			.collect(Collectors.toList());
	}

	private static int findColumn (List<String> header, String... names) {
		for (int i = 0; i < header.size(); i++) {
			String cell = header.get(i).toLowerCase();
			for (String name : names) if (cell.contains(name)) return i;
		}
		return -1;
	}

	private static String cell (List<String> row, int column) {
		return column < row.size() ? row.get(column) : "";
	}

	private static int skipOptional (String text, int i) {
		if (i < text.length() && text.charAt(i) == '[' && text.indexOf(']', i) != -1) return text.indexOf(']', i) + 1;
		return i;
	}

	/**
	 * The text of a LaTeX table cell (e.g. \texttt{\makecell[lt]{7 8 \\ 1 2}}) as it is printed: \\ is a new line,
	 * commands and braces are removed, escaped characters are unescaped and \verb is kept as it is. The result is normalized.
	 */
	public static String toPlainText (String latex) {
		latex = removeTableRules(latex);
		StringBuilder text = new StringBuilder();
		int n = latex.length();
		for (int i = 0; i < n; i++) {
			char current = latex.charAt(i);
			if (current == '\\' && i + 1 < n) {
				char next = latex.charAt(i + 1);
				if (next == '\\') {
					text.append('\n');
					i += 2;
					if (i < n && latex.charAt(i) == '*') i++;
					i = skipOptional(latex, i) - 1;
					continue;
				}
				if (Character.isLetter(next)) {
					int j = i + 1;
					while (j < n && Character.isLetter(latex.charAt(j))) j++;
					String command = latex.substring(i + 1, j);
					if (command.equals("verb") || command.equals("lstinline")) {
						j = skipOptional(latex, j);
						if (j < n) {
							char delimiter = latex.charAt(j) == '{' ? '}' : latex.charAt(j);
							int end = latex.indexOf(delimiter, j + 1);
							if (end == -1) end = n;
							text.append(latex, j + 1, end);
							i = end;
							continue;
						}
					}
					if (SYMBOLS.containsKey(command)) text.append(SYMBOLS.get(command));
					while (j < n && latex.charAt(j) == ' ') j++;
					i = skipOptional(latex, j) - 1;
					continue;
				}
				if (next == ' ' || next == ',' || next == ';' || next == ':') text.append(' ');
				else if (next != '!') text.append(next);
				i++;
				continue;
			}
			if (current == '{' || current == '}' || current == '$') continue;
			if (current == '~' || Character.isWhitespace(current)) text.append(' ');
			else text.append(current);
		}
		return normalize(text.toString());
	}

	/**
	 * The lines of text with every run of white-space in them replaced by one space and trimmed - to compare up to
	 * white-space while the new lines still have to match. The last new line only ends the last line (as in a file),
	 * so it is removed, but any other empty line at the end remains.
	 */
	public static String normalize (String text) {
		text = text.replace("\r", "").replaceAll("[ \\t\\f]+$", "");
		if (text.endsWith("\n")) text = text.substring(0, text.length() - 1);
		List<String> lines = new ArrayList<>();
		for (String line : text.split("\n", -1)) lines.add(line.replaceAll("\\s+", " ").trim());
		return String.join("\n", lines);
	}

	/** All numbers in text, where a-b is the range a, a+1, ..., b (e.g. 0-3,5 or 0-3;5 is 0, 1, 2, 3, 5). */
	public static List<Integer> parseNumbers (String text) {
		List<Integer> numbers = new ArrayList<>();
		if (text == null) return numbers;
		Matcher number = NUMBERS.matcher(text);
		while (number.find()) {
			if (number.group(3) != null) numbers.add(Integer.valueOf(number.group(3)));
			else {
				int from = Integer.parseInt(number.group(1)), to = Integer.parseInt(number.group(2));
				if (to - from > 1000) continue;
				for (int i = from; i <= to; i++) numbers.add(i);
			}
		}
		return numbers;
	}

	/** The first number in text, with . or , (also {,}) as a decimal separator, or null. */
	public static Double parseNumber (String text) {
		if (text == null) return null;
		Matcher number = NUMBER.matcher(text.replaceAll("[{}]", ""));
		if (!number.find()) return null;
		return Double.valueOf(number.group().replace(',', '.'));
	}
}
