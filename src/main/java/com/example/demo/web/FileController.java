package com.example.demo.web;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.utils.WebFluxUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Flux<ServerSentEvent<String>> upload(@RequestPart Flux<FilePart> files) throws IOException {
		return WebFluxUtils.SSE(files.flatMap(this::saveFile));
	}

	private void close(AsynchronousFileChannel fileChannel) {
		try {
			if (fileChannel != null) fileChannel.close();
		} catch (IOException e) {
			log.error("close fileChannel error", e);
		}
	}

	private Publisher<? extends String> saveFile(FilePart file) {
		try {
			final Path path = Paths.get("D:/", "demo", file.filename());
			if (Files.exists(path)) {
				Files.delete(path);
			}
			Files.createFile(path);
			return file.transferTo(path.toFile())
					.map(v -> file.filename());
		} catch (IOException e) {
			return Mono.error(e);
		}
	}

}
