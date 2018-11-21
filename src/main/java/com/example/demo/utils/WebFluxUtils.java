package com.example.demo.utils;

import org.springframework.http.codec.ServerSentEvent;

import reactor.core.publisher.Flux;

public abstract class WebFluxUtils {

	public static <T> Flux<ServerSentEvent<T>> SSE(Flux<T> flux) {
		return flux.map(e -> ServerSentEvent.builder(e)
//				.id(Long.toString(Instant.now().toEpochMilli()))
//				.event("default")
				.build());
	}

}
