package com.example.demo.web;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBufferUtils;
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
	public Flux<ServerSentEvent<String>> upload(@RequestPart Flux<FilePart> files) {
		return WebFluxUtils.SSE(files.flatMap(this::saveFile));
	}

	private Publisher<? extends String> saveFile(FilePart file) {
		try {
			final String filename = file.filename();
			final Path path = Paths.get("D:/", "demo", filename);
			if (Files.exists(path)) {
				Files.delete(path);
			}
			Files.createFile(path);

			final AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
			return DataBufferUtils.write(file.content(), channel, 0)
					.doOnComplete(() -> {
						try {
							channel.close();
						} catch (IOException e) {
							log.error("upload failed", e);
						}
						log.info("uploaded: {}", filename);
					})
					.then(Mono.just(filename));
		} catch (IOException e) {
			return Mono.error(e);
		}
	}

}
