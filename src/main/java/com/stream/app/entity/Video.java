package com.stream.app.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
@Table(name="yt_videos")
public class Video {

	@Id
	private String videoId;
	
	private String title;
	
	private String description;
	
	private String contentType;
	
	private String filePath;
	
	@ManyToOne
	private Course course;
}
