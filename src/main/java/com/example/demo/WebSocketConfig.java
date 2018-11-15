package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.example.demo.socket.ChatHandler;

@Configuration
public class WebSocketConfig {

	@Autowired
	private ChatHandler chatHandler;

	@Bean
	public HandlerMapping webSocketMapping(){
		final Map<String, WebSocketHandler> map = new HashMap<>(1);
		map.put("/socket/chat", chatHandler);
		final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
		mapping.setUrlMap(map);
		return mapping;
	}

	@Bean
	public WebSocketHandlerAdapter webSocketHandlerAdapter(){
		return new WebSocketHandlerAdapter();
	}

}
