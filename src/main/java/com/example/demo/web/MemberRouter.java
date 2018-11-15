package com.example.demo.web;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;

import reactor.core.publisher.Flux;

@Configuration
public class MemberRouter {

	@Autowired
	private MemberRepository memberRepository;

	@Bean
	public RouterFunction<ServerResponse> route() {
		return RouterFunctions
				// 取得全部Member
				.route(RequestPredicates.GET("/member"),
						request -> ServerResponse.ok().body(memberRepository.findAll()
								.map(e -> ServerSentEvent.builder(e).build())
								, new ParameterizedTypeReference<ServerSentEvent<Member>>() {}))

				// 根據EmpNo取得Member
				.andRoute(RequestPredicates.GET("/member/{empNo}"),
						request -> ServerResponse.ok().body(memberRepository.findByEmpNo(Long.valueOf(request.pathVariable("empNo"))), Member.class))

				// 建立一個Member
				.andRoute(RequestPredicates.POST("/member"),
						request -> request.bodyToMono(Member.class)
								.flatMap(memberRepository::save)
								.flatMap(p -> ServerResponse.ok().body(BodyInserters.fromObject(p))))

				// 快速建立{size}個Member
				.andRoute(RequestPredicates.GET("/new/member/{size}"),
						request -> ServerResponse.ok().body(Flux.range(0, Integer.valueOf(request.pathVariable("size")))
										.map(seq -> Member.builder()
												.empNo(Instant.now().toEpochMilli())
												.name(UUID.randomUUID().toString())
												.build())
										.flatMap(memberRepository::save)
										.map(e -> ServerSentEvent.builder(e).build())
								, new ParameterizedTypeReference<ServerSentEvent<Member>>() {}))

				// 修改一個Member
				.andRoute(RequestPredicates.PUT("/member"),
						request -> request.bodyToMono(Member.class)
								.flatMap(member -> memberRepository.findById(member.getId())
										.doOnNext(dbm -> {
											dbm.setEmpNo(member.getEmpNo());
											dbm.setName(member.getName());
										}))
								.flatMap(memberRepository::save)
								.flatMap(p -> ServerResponse.ok().body(BodyInserters.fromObject(p))))

				// 刪除一個Member
				.andRoute(RequestPredicates.DELETE("/member/{empNo}"),
						request -> memberRepository.findByEmpNo(Long.valueOf(request.pathVariable("empNo")))
								.flatMap(memberRepository::delete)
								.flatMap(p -> ServerResponse.ok().body(BodyInserters.fromObject(p))))

				// 刪除全部的Member
				.andRoute(RequestPredicates.DELETE("/member"),
						request -> ServerResponse.ok().body(BodyInserters.fromObject(memberRepository.deleteAll())))

				// Flux Test
				.andRoute(RequestPredicates.GET("/test/flux"),
						request -> ServerResponse.ok().body(
								sse(fluxCreate10StringWithSleep()
										.mergeWith(fluxJust5String())
										.mergeWith(fluxInterval10String())
										.map(s -> "out: " + s)
								)
								, new ParameterizedTypeReference<ServerSentEvent<String>>() {}))

				// Flux AutoConnect Test
				.andRoute(RequestPredicates.GET("/test/autoConnect"),
						request -> ServerResponse.ok().body(
								sse(Flux.from(autoConnect))
								, new ParameterizedTypeReference<ServerSentEvent<String>>() {}));
	}

	private <T> Flux<ServerSentEvent<T>> sse(Flux<T> flux) {
		return flux.map(e -> ServerSentEvent.builder(e).build());
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
