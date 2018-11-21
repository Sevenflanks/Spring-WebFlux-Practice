package com.example.demo.web;

import static com.example.demo.utils.WebFluxUtils.SSE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.entity.Member;
import com.example.demo.handler.MemberHandler;

@Profile("router")
@Configuration
public class MemberRouter {

	@Autowired
	private MemberHandler memberHandler;

	@Bean
	public RouterFunction<ServerResponse> route() {
		return RouterFunctions
				// 取得全部Member
				.route(RequestPredicates.GET("/member"),
						request -> ServerResponse.ok().body(SSE(memberHandler.getAll())
								, new ParameterizedTypeReference<ServerSentEvent<Member>>() {}))

				// 根據EmpNo取得Member
				.andRoute(RequestPredicates.GET("/member/{empNo}"),
						request -> ServerResponse.ok().body(memberHandler.getByEmpNo(Long.valueOf(request.pathVariable("empNo")))
								, Member.class))

				// 建立一個Member
				.andRoute(RequestPredicates.POST("/member"),
						request -> ServerResponse.ok().body(memberHandler.insert(request.bodyToMono(Member.class))
								, Member.class))

				// 快速建立{size}個Member
				.andRoute(RequestPredicates.GET("/member/generate/{size}"),
						request -> ServerResponse.ok().body(SSE(memberHandler.generate(Integer.valueOf(request.pathVariable("size"))))
								, new ParameterizedTypeReference<ServerSentEvent<Member>>() {}))

				// 修改一個Member
				.andRoute(RequestPredicates.PUT("/member"),
						request -> ServerResponse.ok().body(memberHandler.update(request.bodyToMono(Member.class))
								, Member.class))

				// 刪除一個Member
				.andRoute(RequestPredicates.DELETE("/member/{empNo}"),
						request -> ServerResponse.ok().body(memberHandler.delete(Long.valueOf(request.pathVariable("empNo")))
								, Void.class))

				// 刪除全部的Member
				.andRoute(RequestPredicates.DELETE("/member"),
						request -> ServerResponse.ok().body(memberHandler.delete()
								, Void.class));
	}

}
