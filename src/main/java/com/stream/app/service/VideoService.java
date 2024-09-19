package com.stream.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.stream.app.entity.Video;

public interface VideoService {
	
	Video save(Video video,MultipartFile file);

	Video get(String videoId);
	
	Video getTitle(String title);
	
	List<Video> getAll();
}
