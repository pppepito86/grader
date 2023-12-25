package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnalysisFinder {

        public static Path[] find (String statement, List<Path> paths) {
		if (statement!=null) statement=removeExtension(statement.toLowerCase());
		final String statementName=statement;
                paths = paths.stream()
			        .filter(x -> {
					    String name=x.getFileName().toString().toLowerCase();
                                            if ((!name.endsWith("pdf"))&&(!name.endsWith("docx"))&&(!name.endsWith("doc"))) return false;
                                            String path=removeExtension(x.toString().toLowerCase());
                                            if (path.equals(statementName)) return false;
                                            name=removeExtension(name);
                                            if ((!path.contains("analysis"))&&(!path.contains("solution"))&&(!path.contains("author"))&&
                                                (!name.startsWith("sol"))&&(!name.startsWith("resh"))&&
                                                (!name.endsWith("sol"))&&(!name.endsWith("resh"))) return false;
                                            return true;
				}).collect(Collectors.toList());

                for (String s: new String[]{"analysis", "solution"}) {
                        if (paths.stream().filter(f -> removeExtension(f.getFileName().toString().toLowerCase()).contains(s)).count() > 0) {
                                paths = paths.stream().filter(f -> removeExtension(f.getFileName().toString().toLowerCase()).contains(s)).collect(Collectors.toList());
                                break;
                        }
                }

                return paths.stream().toArray(Path[]::new);
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
