package com.github.video.videoopenpose.core.service;

import com.github.video.videoopenpose.core.conf.TaskConf;
import com.github.video.videoopenpose.core.conf.VideoConf;
import com.github.video.videoopenpose.core.task.TaskCore;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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
public class CoreService implements ApplicationRunner {

    @Autowired
    private VideoConf videoConf;

    @Autowired
    private TaskConf taskConf;

    @Autowired
    private ApplicationContext applicationContext;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        nextTask();
    }


    /**
     * 执行下一个任务
     */
    public void nextTask() {
        new Timer().schedule(new VideoTask(), CoreService.this.taskConf.getTaskSleepTime());
    }


    private class VideoTask extends TimerTask {


        @Override
        public void run() {

            //启动线程池持续获取视频流图片
            this.executeVideoCapture();

            //图片
            this.captureToWorkFile();
        }


        private void captureToWorkFile() {
            final File inputFile = new File(taskConf.getWorkFile().getAbsolutePath() + "/input");
            inputFile.mkdirs();
            new Thread(new CaptureToWorkThread()).start();
        }


        @SneakyThrows
        private void executeVideoCapture() {
            final ExecutorService taskPool = Executors.newFixedThreadPool(videoConf.getItems().length);
            final CountDownLatch countDownLatch = new CountDownLatch(videoConf.getItems().length);
            //保存视频里的图片
            final File workImages = new File(taskConf.getWorkFile().getAbsolutePath() + "/capture");
            workImages.mkdirs();
            //执行任务
            Arrays.stream(videoConf.getItems()).forEach((item) -> {
                taskPool.execute(() -> {
                    try {
                        applicationContext.getBean(TaskCore.class).run(workImages, item);
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

    public class CaptureToWorkThread implements Runnable {

        @Override
        public void run() {

        }
    }


}
