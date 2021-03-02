package org.pesho.grader;
import java.io.File;

import org.pesho.grader.task.TaskDetails;
import org.pesho.grader.task.TaskParser;

public class Main {
	
	public static void main(String[] args) {
		TaskParser taskParser = new TaskParser(new File(args[0]));
		TaskDetails taskDetails = new TaskDetails(taskParser);
		System.out.println(taskDetails);
	}

}