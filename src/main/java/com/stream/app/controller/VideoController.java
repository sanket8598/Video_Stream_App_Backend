package com.stream.app.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.stream.app.constants.AppConstants;
import com.stream.app.entity.Video;
import com.stream.app.payload.CustomMessage;
import com.stream.app.service.VideoService;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
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

	// get all videos
	@GetMapping
	public List<Video> getAll() {
		return service.getAll();
	}

	// stream video
	@GetMapping("/stream/{videoId}")
	public ResponseEntity<Resource> stream(@PathVariable String videoId) {
		Video video = service.get(videoId);

		String contentType = video.getContentType();
		String filePath = video.getFilePath();

		if (contentType == null)
			contentType = "application/octet-stream";

		Resource resource = new FileSystemResource(filePath);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.parseMediaType(contentType));
		return new ResponseEntity<>(resource, responseHeaders, HttpStatus.OK);
	}

	// stream video in chunks
	@GetMapping("/stream/range/{videoId}")
	public ResponseEntity<Resource> streamVideoRange(@PathVariable String videoId,
			@RequestHeader(value = "Range", required = false) String range) {
		Video video = service.get(videoId);

		String contentType = video.getContentType();
		String filePath = video.getFilePath();

		if (contentType == null)
			contentType = "application/octet-stream";

		Path path = Paths.get(filePath);
		Resource resource = new FileSystemResource(path);

		long fileLength = path.toFile().length();

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.parseMediaType(contentType));
		if (range == null) {
			return new ResponseEntity<>(resource, responseHeaders, HttpStatus.OK);
		}

		long rangeStart;
		long rangeEnd;

		String[] ranges = range.replace("bytes=", "").split("-");
		rangeStart = Long.parseLong(ranges[0]);

		rangeEnd = rangeStart + AppConstants.CHUNK_SIZE - 1;
		if (rangeEnd > fileLength - 1) {
			rangeEnd = fileLength - 1;
		}
		// here we were caluculating the endRange
		/*
		 * if(ranges.length>1) { rangeEnd=Long.parseLong(ranges[1]); }else {
		 * rangeEnd=fileLength-1; }
		 * 
		 * System.out.println(rangeStart+"  "+rangeEnd); if(rangeEnd > fileLength-1) {
		 * rangeEnd=fileLength-1; }
		 */

		InputStream inputStream;

		try {
			inputStream = Files.newInputStream(path);
			inputStream.skip(rangeStart);

			long contentLenght = rangeEnd - rangeStart + 1;
			
			byte[] data=new byte[(int)contentLenght];
			
			int read = inputStream.read(data,0,data.length);
            System.out.println("No of bytes Read: "+read);
			
			responseHeaders.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
			responseHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
			responseHeaders.add("Pragma", "no-cache");
			responseHeaders.add("Expires", "0");
			responseHeaders.add("X-Content-Type-Options", "nosniff");
			responseHeaders.setContentLength(contentLenght);
//			return new ResponseEntity<>(new InputStreamResource(inputStream), responseHeaders,
//					HttpStatus.PARTIAL_CONTENT);
			return new ResponseEntity<>(new ByteArrayResource(data), responseHeaders,
					HttpStatus.PARTIAL_CONTENT);

		} catch (IOException ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}
	
	//serve hls playlist
    @Value("${file.video.hsl}")	
	private String HLS_DIR;
	
	@GetMapping("/{videoId}/master.m3u8")
	public ResponseEntity<Resource> serveMasterFile(@PathVariable String videoId){
		
		//creating path
		Path path = Paths.get(HLS_DIR,videoId,"master.m3u8");
		System.out.println(path);
		if(Files.exists(path)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Resource resource=new FileSystemResource(path);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"));
		return new  ResponseEntity<>(resource, responseHeaders,
				HttpStatus.OK);
		
	}
	
	//serve the segments
	@GetMapping("/{videoId}/{segment}.ts")
	public ResponseEntity<Resource> serveSegments(@PathVariable String videoId,@PathVariable String segment){
		//create path for segment
		
		Path path=Paths.get(HLS_DIR,videoId,segment+".ts");
		System.out.println(path);
		if(Files.exists(path)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Resource resource=new FileSystemResource(path);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.parseMediaType("video/mp2t"));
		return new  ResponseEntity<>(resource, responseHeaders,
				HttpStatus.OK);
		
	}
}
