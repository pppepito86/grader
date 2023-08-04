package org.pesho.grader.task.quiz;

public class Quiz {
	
	private QuizTask[] tasks;
	private String[] options;

	public QuizTask[] getTasks() {
		return tasks;
	}

	public void setTasks(QuizTask[] tasks) {
		this.tasks = tasks;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	public Quiz clone() {
		Quiz quiz = new Quiz();
		quiz.tasks = new QuizTask[this.tasks.length];
		for (int i = 0; i < this.tasks.length; i++) quiz.tasks[i] = this.tasks[i].clone();
		return quiz;
	}

}
