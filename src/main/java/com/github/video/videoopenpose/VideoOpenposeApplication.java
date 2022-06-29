package com.github.video.videoopenpose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("com.github.video.videoopenpose.core")
public class VideoOpenposeApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoOpenposeApplication.class, args);
    }

}
