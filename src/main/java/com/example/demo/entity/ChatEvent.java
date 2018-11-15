package com.example.demo.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatEvent {

	private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

	public enum Type {
		MESSAGE, JOIN, LEAVE, LOGIN
	}

	private String id;
	private Type type;
	private LocalDateTime time;
	private String message;

	public ChatEvent(String id) {
		this.id = id;
		this.time = LocalDateTime.now();
	}

	public ChatEvent(String id, Type type) {
		this(id);
		this.type = type;
	}

	public ChatEvent(String id, String message) {
		this(id, Type.MESSAGE);
		this.message = message;
	}

	@Override
	public String toString() {
		final String timeTx = TIME_FMT.format(this.time);
		switch(type) {
			case MESSAGE: return id + ": " + message + " (" + timeTx + ")";
			case JOIN: return id + " joined (" + timeTx + ")";
			case LEAVE: return id + " leaved (" + timeTx + ")";
			default: return "";
		}
	}
}
