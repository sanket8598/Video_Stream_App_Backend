package com.stream.app.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="yt_cources")
public class Course {
	
	@Id
	private String id;
	
	private String title;
	
	@OneToMany(mappedBy = "course")
	private List<Video> list=new ArrayList<>();

}
