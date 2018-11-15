package com.example.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.demo.entity.Member;

import reactor.core.publisher.Mono;

public interface MemberRepository extends ReactiveMongoRepository<Member, String> {

	Mono<Member> findByEmpNo(Long empNo);
}
