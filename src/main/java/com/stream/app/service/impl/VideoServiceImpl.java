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
		File file=new File(DIR);
		if(!file.exists()) {
			file.mkdir();
			System.out.println("Folder Created");
		}else
			System.out.println("Folder Already Created");
	}

	@Override
	public Video save(Video video, MultipartFile file) {
	try {
		String fileName=file.getOriginalFilename();
		String contentType=file.getContentType();
		InputStream inputStream=file.getInputStream();
		
		String cleanFileName = StringUtils.cleanPath(fileName);
		String cleanFolder=StringUtils.cleanPath(DIR);
		
		Path path = Paths.get(cleanFolder,cleanFileName);
		System.out.println(path);
		
		Files.copy(inputStream, path,StandardCopyOption.REPLACE_EXISTING);
		
		video.setContentType(contentType);
		video.setFilePath(path.toString());
		
		return repository.save(video);
	}catch(IOException e) {
		e.printStackTrace();
		return null;
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
