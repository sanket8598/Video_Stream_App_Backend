package com.stream.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stream.app.entity.Video;

public interface VideoRepository extends JpaRepository<Video, String>{

	Optional<Video> findByTitle(String title);
}
