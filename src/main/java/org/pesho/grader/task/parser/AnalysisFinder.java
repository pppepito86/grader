package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnalysisFinder {

        public static Optional<Path> find (String statement, List<Path> paths) {
		if (statement!=null) statement=removeExtension(statement.toLowerCase());
		final String statementName=statement;
                paths = paths.stream()
			        .filter(x -> {
					    String name=x.getFileName().toString().toLowerCase();
                                            if ((!name.endsWith("pdf"))&&(!name.endsWith("docx"))&&(!name.endsWith("doc"))&&(!name.endsWith("rtf"))) return false;
                                            String path=removeExtension(x.toString().toLowerCase());
                                            if (path.equals(statementName)) return false;
                                            name=removeExtension(name);
                                            if ((!path.contains("analysis"))&&(!path.contains("solution"))&&(!path.contains("author"))&&
						(!path.contains("analiz"))&&(!path.contains("reshenie"))&&
                                                (!name.startsWith("sol"))&&(!name.startsWith("resh"))&&
                                                (!name.endsWith("sol"))&&(!name.endsWith("resh"))) return false;
                                            return true;
				}).collect(Collectors.toList());

                if (paths.size() == 0) return Optional.empty();
		
		for (String s: new String[]{"analysis", "solution", "author", "analiz", "reshenie"}) {
                        if (paths.stream().filter(f -> removeExtension(f.getFileName().toString().toLowerCase()).contains(s)).count() > 0) {
                                paths = paths.stream().filter(f -> removeExtension(f.getFileName().toString().toLowerCase()).contains(s)).collect(Collectors.toList());
                                break;
                        }
                }
		
		paths.sort((a, b) -> a.toString().length() - b.toString().length());
		for (Path p : paths) {
		        if (p.getFileName().toString().endsWith("pdf")) return Optional.ofNullable(p);
		}
                return paths.stream().findFirst();
        }

	private static String removeExtension (String name) {
	        String[] parts=name.split(".");
		if (parts.length<2) return name;
		String res="";
		for (int i=0; i<=parts.length-2; i++) {
		    res+=parts[i];
		    if (i<parts.length-2) res+=".";
		}
                return res;
        }

}
