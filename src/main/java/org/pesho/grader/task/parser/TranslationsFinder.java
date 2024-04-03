package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class TranslationsFinder {

    public static List<Path> find(String statement, List<Path> paths) {
        if (statement == null) return new ArrayList<>();
        String dir = (new File(statement)).getAbsoluteFile().getParent();
        return paths.stream()
            .filter(x -> {
                String path = x.toString();
                if (!path.endsWith("pdf")) return false;
                if (!(new File(path)).getAbsoluteFile().getParent().equals(dir)) return false;
                int len = path.length();
                if (len < 7) return false;
                if (path.charAt(len-7) == '_' && 
                    Character.isLowerCase(path.charAt(len-6)) && Character.isLowerCase(path.charAt(len-5))) return true;
                return false;
            }).collect(Collectors.toList());
    }

}
