package com.github.video.videoopenpose.core.service;

import com.github.video.videoopenpose.core.conf.TaskConf;
import com.github.video.videoopenpose.core.conf.VideoConf;
import com.github.video.videoopenpose.core.helper.FFMPEGHelper;
import lombok.SneakyThrows;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CoreService {

    @Autowired
    private VideoConf videoConf;

    @Autowired
    private TaskConf taskConf;

    @Autowired
    private ApplicationContext applicationContext;


    @Autowired
    private void init(ApplicationContext applicationContext) {
        nextTask();
    }


    /**
     * 执行下一个任务
     */
    public void nextTask() {
        new Timer().schedule(new VideoTask(), CoreService.this.taskConf.getTaskSleepTime());
    }


    /**
     * 执行单个任务
     *
     * @param item
     */
    @SneakyThrows
    private void executeTask(VideoConf.Item item) {
        FFMPEGHelper ffmpegHelper = applicationContext.getBean(FFMPEGHelper.class);
        ffmpegHelper.setItem(item);
        ffmpegHelper.run();
    }


    private class VideoTask extends TimerTask {


        @Override
        public void run() {
            try {
                execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            CoreService.this.nextTask();
        }


        @SneakyThrows
        private void execute() {
            final ExecutorService taskPool = Executors.newFixedThreadPool(taskConf.getTaskPoolCount());
            final CountDownLatch countDownLatch = new CountDownLatch(videoConf.getItems().length);


            //执行任务
            Arrays.stream(videoConf.getItems()).forEach((item) -> {
                taskPool.execute(() -> {
                    try {
                        CoreService.this.executeTask(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            });

            countDownLatch.await();
            taskPool.shutdownNow();
        }
    }


}
