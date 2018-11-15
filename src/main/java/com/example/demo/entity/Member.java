package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Document
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member implements Persistable<String> {

	@Id
	private String id;
	@CreatedDate
	private LocalDateTime createdTime;
	@LastModifiedDate
	private LocalDateTime updateTime;
	private Long empNo;
	private String name;

	@Override
	public boolean isNew() {
		return Objects.isNull(id);
	}
}
