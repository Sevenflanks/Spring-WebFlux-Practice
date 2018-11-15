package com.example.demo.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.example.demo.entity.ChatEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

@Slf4j
@Component
public class ChatHandler implements WebSocketHandler {

	@Autowired
	private ObjectMapper objectMapper;

	private final ReplayProcessor<ChatEvent> eventPublisher;
	private final Flux<ChatEvent> output;

	public ChatHandler() {
		this.eventPublisher = ReplayProcessor.create(0);
		this.output = Flux.from(eventPublisher.replay(0).autoConnect());
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		final String sessionId = session.getId();

		// 當從client發送訊息時的處理
		final Flux<ChatEvent> publishReceved = session.receive().log()
				.doOnComplete(() -> eventPublisher.onNext(new ChatEvent(sessionId, ChatEvent.Type.LEAVE)))
				.doOnSubscribe(s -> {
					eventPublisher.subscribe();
					eventPublisher.onNext(new ChatEvent(sessionId, ChatEvent.Type.JOIN));
				})
				.map(WebSocketMessage::getPayloadAsText)
				.map(msg -> new ChatEvent(sessionId, msg))
				.doOnNext(eventPublisher::onNext);

		// 當建立Session時回應sessionId給該Session的client (其他人不會看到此訊息)
		final Mono<Void> responseId = session.send(Mono.just(new ChatEvent(sessionId, ChatEvent.Type.LOGIN))
				.map(this::toJson)
				.map(session::textMessage));

		// 當訊息被publish到串流中的時候，要丟給client端的處理
		final Mono<Void> responseChat = session.send(output.takeUntil(e -> sessionLeave(sessionId, e))
				.map(this::toJson)
				.map(session::textMessage));

		return responseId.and(publishReceved).and(responseChat);
	}

	private String toJson(ChatEvent value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			log.error("toJson Failed, value:{}", value, e);
			return null;
		}
	}

	// 當通道上出現相同SessionID發出Leave事件時，中斷通道
	private boolean sessionLeave(String sessionId, ChatEvent e) {
		return e.getType() == ChatEvent.Type.LEAVE && sessionId.equals(e.getId());
	}

}
