package org.pesho.grader.task.quiz;

public class Quiz {
	
	private QuizTask[] tasks;

	private Boolean isShuffleEnabled;

	public QuizTask[] getTasks() {
		return tasks;
	}
	
	public void setTasks(QuizTask[] tasks) {
		this.tasks = tasks;
	}

	public Boolean getShuffleEnabled() {
		return isShuffleEnabled;
	}

	public void setShuffleEnabled(Boolean shuffleEnabled) {
		isShuffleEnabled = shuffleEnabled;
	}

	public Quiz clone() {
		Quiz quiz = new Quiz();
		quiz.tasks = new QuizTask[this.tasks.length];
		for (int i = 0; i < this.tasks.length; i++) quiz.tasks[i] = this.tasks[i].clone();
		quiz.isShuffleEnabled = this.isShuffleEnabled;
		return quiz;
	}

}
