package org.pesho.grader.task.quiz;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuizTask {

	private String title;
	private QuizType type;
	private String question;
	private String description;
	private QuizImage image;
	private String[] answers;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)

	private String[] correctAnswers;
	private double points;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public QuizType getType() {
		return type;
	}
	
	public void setType(QuizType type) {
		this.type = type;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public QuizImage getImage() {
		return image;
	}

	public void setImage(QuizImage image) {
		this.image = image;
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
	
	public double getPoints() {
		return points;
	}
	
	public void setPoints(double points) {
		this.points = points;
	}

	public QuizTask clone() {
		QuizTask task = new QuizTask();
		if (this.title != null) task.title = this.title;
		task.type = this.type;
		if (this.question != null) task.question = this.question;
		if (this.description != null) task.description = this.description;
		if (this.image != null) task.image = this.image;
		if (this.answers != null) task.answers = this.answers.clone();
		task.points = this.points;
		return task;
	}

}
