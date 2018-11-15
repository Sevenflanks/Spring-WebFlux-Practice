package com.example.demo.handler;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MemberHandler {

	@Autowired
	private MemberRepository memberRepository;

	public Flux<Member> getAll() {
		return memberRepository.findAll();
	}

	public Mono<Member> getByEmpNo(Long empNo) {
		return memberRepository.findByEmpNo(empNo);
	}

	public Mono<Member> insert(Mono<Member> member) {
		return member.flatMap(memberRepository::save);
	}

	public Flux<Member> generate(int size) {
		return Flux.range(0, size)
				.map(seq -> Member.builder()
						.empNo(Instant.now().toEpochMilli())
						.name(UUID.randomUUID().toString())
						.build())
				.flatMap(memberRepository::save);
	}

	public Mono<Member> update(Mono<Member> member) {
		return member.flatMap(m -> memberRepository.findById(m.getId())
				.doOnNext(dbm -> {
					dbm.setEmpNo(m.getEmpNo());
					dbm.setName(m.getName());
				}))
				.flatMap(memberRepository::save);
	}

	public Mono<Void> delete(Long empNo) {
		return memberRepository.findByEmpNo(empNo)
				.flatMap(memberRepository::delete);
	}

	public Mono<Void> delete() {
		return memberRepository.deleteAll();
	}

}
