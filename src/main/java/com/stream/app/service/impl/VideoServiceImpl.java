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

	@Value("${file.video.hsl}")
	private String HSL_DIR;

	@PostConstruct
	public void init() {

		File file = new File(DIR);

		try {
			Files.createDirectories(Paths.get(HSL_DIR));
		} catch (IOException e) {
			e.printStackTrace();
		}
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

			// folder path : create

			String cleanFolder = StringUtils.cleanPath(DIR);

			// folder path with filename
			Path path = Paths.get(cleanFolder, cleanFileName);

			System.out.println(contentType);
			System.out.println(path);

			// copy file to the folder
			Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

			// video meta data

			video.setContentType(contentType);
			video.setFilePath(path.toString());
			
			
			Video savedVideo = repository.save(video);
			// processing video
			 processVideo(savedVideo.getVideoId());

			// delete actual video file and database entry if exception

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
		return repository.findAll();
	}

	@Override
	public String processVideo(String videoId) {

		Video video = this.get(videoId);
		String filePath = video.getFilePath();

		// where to store data
		Path videoPath = Paths.get(filePath);

		/*
		 * String output360p = HSL_DIR + videoId + "/360p/"; String output720p = HSL_DIR
		 * + videoId + "/720p/"; String output1080p = HSL_DIR + videoId + "/1080p/";
		 */
		try {
			/*
			 * Files.createDirectories(Paths.get(output360p));
			 * Files.createDirectories(Paths.get(output720p));
			 * Files.createDirectories(Paths.get(output1080p));
			 */

			Path outputPath = Paths.get(HSL_DIR, videoId);
			Files.createDirectories(outputPath);
			// ffmpeg cmd

			String ffmPegCmd = String.format(
					"ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\" \"%s/matser.m3u8\"",
					videoPath, outputPath, outputPath);

			/*
			 * StringBuilder ffmPegCmd=new StringBuilder(); ffmPegCmd.append("ffmpeg -i");
			 * ffmPegCmd.append(videoPath.toString()) .append(" -c:v libx264 -c:a aac")
			 * .append(" ") .append("-map 0:v -map 0:a -s:v:0 640*360 -b:v:0 800k ")
			 * .append("-map 0:v -map 0:a -s:v:1 1280*720 -b:v:1 2800k ")
			 * .append("-map 0:v -map 0:a -s:v:2 1920*1080 -b:v:2 5000k ")
			 * .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
			 * .append("-master_pl_name ").append(HSL_DIR) .append(videoId)
			 * .append("/master.m3u8 ") .append("-f hls_time 10 -hls_list_size 0 ")
			 * .append("-hls_segment_filename \"") .append(HSL_DIR) .append(videoId)
			 * .append("/v%v/file") .append("\"").append(HSL_DIR).append(videoId)
			 * .append("/v%v/prog_index.m3u8\"");
			 */
			ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmPegCmd);
			processBuilder.inheritIO();
			Process process = processBuilder.start();
			int exit = process.waitFor();
			if (exit != 0) {
				throw new RuntimeException("Video processing failed");
			}
			return videoId;
		} catch (IOException | InterruptedException ex) {
			throw new RuntimeException("Video processing failed");
		}

	}

}
