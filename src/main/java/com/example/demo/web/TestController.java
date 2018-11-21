package com.example.demo.web;

import static com.example.demo.utils.WebFluxUtils.SSE;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

@RestController
@RequestMapping("/test")
public class TestController {

	@GetMapping("/push")
	public Flux<ServerSentEvent<Integer>> push() {
		return Flux.interval(Duration.ofSeconds(1)).take(10)
				.map(seq -> Tuples.of(seq, ThreadLocalRandom.current().nextInt()))
				.map(data -> ServerSentEvent.<Integer>builder()
						.event("random")
						.id(Long.toString(data.getT1()))
						.data(data.getT2())
						.build());
	}

	@GetMapping("/flux")
	public Flux<ServerSentEvent<String>> flux() {
		return SSE(fluxCreate10StringWithSleep()
				.mergeWith(fluxJust5String())
				.mergeWith(fluxInterval10String())
				.map(s -> "out: " + s));
	}

	@GetMapping("/autoConnect")
	public Flux<ServerSentEvent<String>> autoConnect() {
		return SSE(Flux.from(autoConnect));
	}

	static Flux<String> autoConnect = Flux.interval(Duration.ofMillis(100))
			.map(Object::toString)
			.publish()
			.autoConnect();

	private Flux<String> fluxInterval10String() {
		return Flux.interval(Duration.ofMillis(100))
				.take(10)
				.map(Object::toString);
	}

	private Flux<String> fluxJust5String() {
		return Flux.just("A", "B", "C", "D", "E");
	}

	private Flux<String> fluxCreate10StringWithSleep() {
		return Flux.create(sink -> {
			String last = UUID.randomUUID().toString();
			int i = 0;
			while (i++ < 10) {
				sleep(100); // not nonblocking
				sink.next(last);
				last = run(last);
			}
			sink.complete();
		})
				.map(Object::toString);
	}

	private String run(String in) {
		return in + ">" + UUID.randomUUID();
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException();
		}
	}

}
