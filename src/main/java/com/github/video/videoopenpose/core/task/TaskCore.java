package com.github.video.videoopenpose.core.task;

import com.github.video.videoopenpose.core.conf.TaskConf;
import com.github.video.videoopenpose.core.conf.VideoConf;
import com.github.video.videoopenpose.core.helper.PathHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.guieffect.qual.AlwaysSafe;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
@Scope("prototype")
public class TaskCore {

    @Setter
    @Getter
    private boolean running = true;

    @Autowired
    private TaskConf taskConf;

    @Autowired
    private PathHelper pathHelper;


    private VideoConf.Item item;

    //工作空间
    private File workPath;


    @SneakyThrows
    public void run(File workPath, VideoConf.Item item) {
        this.workPath = workPath;
        this.item = item;
        //阻塞器
        //创建启动任务
        final FFmpegJob fFmpegJob = createJob();

        //启动任务
        runJob(fFmpegJob);
    }


    @SneakyThrows
    private void runJob(FFmpegJob fFmpegJob) {
        try {
            fFmpegJob.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (running) {
                Thread.sleep(taskConf.getTaskSleepTime());
                runJob(fFmpegJob);
            }
        }
    }


    @SneakyThrows
    private FFmpegJob createJob() {
        log.info("video : {}", item.getUrl());
        File imageFile = new File(workPath.getAbsolutePath() + "/" + item.getName());
        if (!imageFile.exists()) {
            imageFile.mkdirs();
        }
        FileUtils.cleanDirectory(imageFile);


        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(item.getUrl())     // Filename, or a FFmpegProbeResult
                .overrideOutputFiles(true) // Override the output if it exists
                .addOutput(imageFile.getAbsolutePath() + "\\%d.jpg")   // Filename for the destination
//                .setFrames(1)
                .addExtraArgs("-vf", String.format("fps=1/%s", taskConf.getCaptureTime()), "-vsync", "0")
                .done();
        FFmpeg ffmpeg = new FFmpeg(pathHelper.getFfmpegFile().getAbsolutePath());
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
        return executor.createJob(builder);
    }


}
