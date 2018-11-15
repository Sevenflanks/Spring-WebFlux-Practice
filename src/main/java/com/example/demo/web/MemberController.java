package com.example.demo.web;

import static com.example.demo.utils.WebFluxUtils.SSE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Member;
import com.example.demo.handler.MemberHandler;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("controller")
@RestController
@RequestMapping("/member")
public class MemberController {

	@Autowired
	private MemberHandler memberHandler;

	/** 取得全部Member */
	@GetMapping
	public Flux<ServerSentEvent<Member>> queryAll() {
		return SSE(memberHandler.getAll());
	}

	/** 根據EmpNo取得Member */
	@GetMapping("/{empNo}")
	public Mono<Member> queryByEmpNo(@PathVariable Long empNo) {
		return memberHandler.getByEmpNo(empNo);
	}

	/** 建立一個Member */
	@PostMapping
	public Mono<Member> insert(@RequestBody Mono<Member> member) {
		return memberHandler.insert(member);
	}

	/** 快速建立{size}個Member */
	@GetMapping("/generate/{size}")
	public Flux<ServerSentEvent<Member>> insert(@PathVariable Integer size) {
		return SSE(memberHandler.generate(size));
	}

	/** 修改一個Member */
	@PutMapping
	public Mono<Member> update(@RequestBody Mono<Member> member) {
		return memberHandler.update(member);
	}

	/** 刪除一個Member */
	@DeleteMapping("/{empNo}")
	public Mono<Void> deleteByEmpNo(@PathVariable Long empNo) {
		return memberHandler.delete(empNo);
	}

	/** 刪除全部的Member */
	@DeleteMapping
	public Mono<Void> deleteAll() {
		return memberHandler.delete();
	}

}
