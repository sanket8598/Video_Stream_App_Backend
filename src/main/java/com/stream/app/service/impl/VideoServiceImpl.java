package com.stream.app.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.stream.app.entity.Video;
import com.stream.app.repository.VideoRepository;
import com.stream.app.service.VideoService;

@Service
public class VideoServiceImpl implements VideoService {
	
	@Autowired
	private VideoRepository repository;
	
	@Value("${files.video}")
	private String DIR;

	@PostConstruct
	public void init() {

		File file = new File(DIR);

		if (!file.exists()) {
			file.mkdir();
			System.out.println("Folder Created:");
		} else {
			System.out.println("Folder already created");
		}

	}

	@Override
	public Video save(Video video, MultipartFile file) {
		// original file name

		try {


			String filename = file.getOriginalFilename();
			String contentType = file.getContentType();
			InputStream inputStream = file.getInputStream();


			// file path
			String cleanFileName = StringUtils.cleanPath(filename);


			//folder path : create

			String cleanFolder = StringUtils.cleanPath(DIR);


			// folder path with  filename
			Path path = Paths.get(cleanFolder, cleanFileName);

			System.out.println(contentType);
			System.out.println(path);

			// copy file to the folder
			Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);


			// video meta data

			video.setContentType(contentType);
			video.setFilePath(path.toString());
			Video savedVideo = repository.save(video);
			//processing video
		//	processVideo(savedVideo.getVideoId());

			//delete actual video file and database entry  if exception

			// metadata save
			return savedVideo;

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error in processing video ");
		}


	}


	@Override
	public Video get(String videoId) {
		return repository.findById(videoId).orElse(null);
	}

	@Override
	public Video getTitle(String title) {
		return null;
	}

	@Override
	public List<Video> getAll() {
		return null;
	}

}
