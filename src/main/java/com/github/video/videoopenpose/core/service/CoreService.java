package com.github.video.videoopenpose.core.service;

import com.github.video.videoopenpose.core.conf.TaskConf;
import com.github.video.videoopenpose.core.conf.VideoConf;
import com.github.video.videoopenpose.core.task.TaskCore;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
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
    private void executeTask(File workPath, VideoConf.Item item) {
        applicationContext.getBean(TaskCore.class).run(workPath, item);
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
            //保存视频里的图片
            final File workImages = new File(taskConf.getWorkFile().getAbsolutePath() + "/images");
            workImages.mkdirs();
            //执行任务
            Arrays.stream(videoConf.getItems()).forEach((item) -> {
                taskPool.execute(() -> {
                    try {
                        CoreService.this.executeTask(workImages, item);
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
