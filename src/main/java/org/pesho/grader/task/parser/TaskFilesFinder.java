package org.pesho.grader.task.parser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.text.SimpleDateFormat;

public class TaskFilesFinder {
	
    public static Map<String, Object> find(String taskName, Path taskDir, List<Path> paths) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> rootMap = new HashMap<>();
		rootMap.put("name", "");
		rootMap.put("path", "");
		rootMap.put("children", new ArrayList<String>());
		rootMap.put("size", 0);
		rootMap.put("modified", "");
		rootMap.put("isFile", false);
		map.put("", rootMap);
		
		Map<String, Object> parentMap = new HashMap<>();
		parentMap.put("name", "..");
		parentMap.put("path", "..");
		parentMap.put("children", new ArrayList<String>());
		parentMap.put("size", 0);
		parentMap.put("modified", "");
		parentMap.put("isFile", false);
		map.put("..", parentMap);
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy HH:mm");
		
		try {
			for (Path path: paths) {
				Path parentPath = Optional.ofNullable(path.getParent()).orElse(Paths.get(taskName+".zip"));
				if (path.toString().isEmpty()) {
					path = Paths.get(taskName+".zip");
					parentPath = Paths.get("");
				}
				Map<String, Object> pathMap = new HashMap<>();
				pathMap.put("name", path.getFileName().toString());
				pathMap.put("parent", parentPath.toString());
				pathMap.put("parent_name", parentPath.getFileName().toString());
				if (path.toString().equals(taskName+".zip")) {
					pathMap.put("path", "");
					pathMap.put("modified", "");
				} else {
					pathMap.put("path", path.toString());
					pathMap.put("modified", sdf.format(taskDir.resolve(path).toFile().lastModified()));
				}
				List<String> children = new ArrayList<>();
				pathMap.put("children", children);
				pathMap.put("size", taskDir.resolve(path).toFile().length());
				pathMap.put("isFile", taskDir.resolve(path).toFile().isFile());
				map.put(path.toString(), pathMap);
				
				if (path.toString().equals(taskName+".zip") || taskDir.resolve(path).toFile().isDirectory()) {
					children.add("..");
				}
				((List<String>) ((Map<String, Object>) map.get(parentPath.toString())).get("children")).add(path.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
    }

}
