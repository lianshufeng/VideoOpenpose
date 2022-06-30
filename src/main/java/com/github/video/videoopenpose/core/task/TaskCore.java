package com.github.video.videoopenpose.core.task;

import com.github.video.videoopenpose.core.conf.VideoConf;
import com.github.video.videoopenpose.core.helper.PathHelper;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@Scope("prototype")
public class TaskCore {

    @Autowired
    private PathHelper pathHelper;


    private VideoConf.Item item;

    //工作空间
    private File workPath;


    public void run(File workPath, VideoConf.Item item) {
        this.workPath = workPath;
        this.item = item;
        execute();
    }


    @SneakyThrows
    private void execute() {
        log.info("video : {}", item.getUrl());


        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(item.getUrl())     // Filename, or a FFmpegProbeResult
                .overrideOutputFiles(true) // Override the output if it exists
                .addOutput(String.format("%s/%s", workPath.getAbsolutePath(), item.getName() + ".jpg"))   // Filename for the destination
                .setFrames(1)
                .done();
        FFmpeg ffmpeg = new FFmpeg(pathHelper.getFfmpegFile().getAbsolutePath());
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
        executor.createJob(builder).run();
//        executor.createTwoPassJob(builder).run();

    }


}
