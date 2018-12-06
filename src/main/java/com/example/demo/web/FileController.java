package com.example.demo.web;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${file.stored.root}")
	private String fileStoredRoot;

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Flux<ServerSentEvent<String>> upload(@RequestPart Flux<FilePart> files) {
		return WebFluxUtils.SSE(files.flatMap(this::saveFile));
	}

	private Publisher<? extends String> saveFile(FilePart file) {
		try {
			// 建立檔案，若存在則先移除
			final String filename = file.filename();
			final Path root = Paths.get(fileStoredRoot);
			if (!Files.exists(root)) {
				Files.createDirectories(root);
			}

			final Path path = root.resolve(filename);
			if (Files.exists(path)) {
				Files.delete(path);
			}
			Files.createFile(path);

			final AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
			return DataBufferUtils.write(file.content(), channel, 0)
					.doOnComplete(() -> {
						try {
							// 每次上傳完需要關閉channel
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
