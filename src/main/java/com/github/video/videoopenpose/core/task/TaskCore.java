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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@Scope("prototype")
public class TaskCore {

    protected final static String ImageExtName = "jpg";

    @Setter
    @Getter
    private boolean running = true;

    @Autowired
    private TaskConf taskConf;

    @Autowired
    private PathHelper pathHelper;

    private VideoConf.Item item;

    //工作空间
    private File workCapture;
    private File workOpenposeInput;

    //保存截图的工作空间
    private File workImageFile;

    @SneakyThrows
    public void run(File workCapture, File workOpenposeInput, VideoConf.Item item) {
        this.workCapture = workCapture;
        this.workOpenposeInput = workOpenposeInput;
        this.item = item;

        //当前保存的路径
        this.workImageFile = new File(workCapture.getAbsolutePath() + "/" + item.getName());
        if (workImageFile.exists()) {
            FileUtils.cleanDirectory(workImageFile);
        } else {
            workImageFile.mkdirs();
        }

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


        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(item.getUrl())     // Filename, or a FFmpegProbeResult
                .overrideOutputFiles(true) // Override the output if it exists
                .addOutput(workImageFile.getAbsolutePath() + "\\%d." + ImageExtName)   // Filename for the destination
//                .setFrames(1)
                .addExtraArgs("-vf", String.format("fps=1/%s", taskConf.getCaptureTime()), "-vsync", "0")
                .done();
        FFmpeg ffmpeg = new FFmpeg(pathHelper.getFfmpegFile().getAbsolutePath());
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
        return executor.createJob(builder);
    }


}
