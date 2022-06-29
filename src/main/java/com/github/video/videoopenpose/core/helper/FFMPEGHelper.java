package com.github.video.videoopenpose.core.helper;

import com.github.video.videoopenpose.core.conf.FFMPEGConf;
import com.github.video.videoopenpose.core.conf.VideoConf;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;

@Slf4j
@Component
@Scope("prototype")
public class FFMPEGHelper implements Runnable {

    @Setter
    private VideoConf.Item item;

    @Autowired
    private FFMPEGConf ffmpegConf;


    @Override
    public void run() {
        execute();
    }


    @Autowired
    @SneakyThrows
    private void init(ApplicationContext applicationContext) {

    }

    @SneakyThrows
    private void execute() {
        log.info("url - {}", item.getUrl());
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(item.getUrl())     // Filename, or a FFmpegProbeResult
                .overrideOutputFiles(true) // Override the output if it exists
                .addOutput(String.format("c:/output/%s", item.getName() + ".jpg"))   // Filename for the destination
                .done();
        FFmpeg ffmpeg = new FFmpeg(ffmpegConf.toFFMPEGFile().getAbsolutePath());
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
        executor.createJob(builder).run();
        executor.createTwoPassJob(builder).run();
    }
}
