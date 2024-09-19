package com.stream.app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.stream.app.entity.Video;
import com.stream.app.payload.CustomMessage;
import com.stream.app.service.VideoService;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

	@Autowired
	private VideoService service;

	@PostMapping
	public ResponseEntity<?> create(@RequestParam("file") MultipartFile file, @RequestParam("title") String title,
			@RequestParam("description") String description) {

		Video video = new Video();
		video.setTitle(title);
		video.setDescription(description);
		video.setVideoId(UUID.randomUUID().toString());
		Video save = service.save(video, file);
		if (save != null)
			return ResponseEntity.status(HttpStatus.OK).body(save);
		return ResponseEntity.ok(CustomMessage.builder().success(true).message("Video Added Successfully").build());
	}
}
