package org.pesho.grader.task.quiz;

public class QuizTask {
	
	private QuizType type;
	private String description;
	private String[] answers;
	private String[] correctAnswers;
	
	public QuizType getType() {
		return type;
	}
	
	public void setType(QuizType type) {
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String[] getAnswers() {
		return answers;
	}
	
	public void setAnswers(String[] answers) {
		this.answers = answers;
	}
	
	public String[] getCorrectAnswers() {
		return correctAnswers;
	}
	
	public void setCorrectAnswers(String[] correctAnswers) {
		this.correctAnswers = correctAnswers;
	}

}
