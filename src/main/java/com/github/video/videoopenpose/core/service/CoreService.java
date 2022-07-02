package com.github.video.videoopenpose.core.service;

import com.github.video.videoopenpose.core.conf.TaskConf;
import com.github.video.videoopenpose.core.conf.VideoConf;
import com.github.video.videoopenpose.core.task.TaskCore;
import com.github.video.videoopenpose.core.task.UpdateCaptureAndOpenpose;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
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


    private File workCapture;

    private File workOpenpose;


    private File workOpenposeInput;
    private File workOpenposeOutput;


    @Override
    public void run(ApplicationArguments args) throws Exception {

        //保存视频里的图片
        workCapture = new File(taskConf.getWorkFile().getAbsolutePath() + "/capture");
        if (!workCapture.exists()) {
            workCapture.mkdirs();
        }


        //工作空间
        workOpenpose = new File(taskConf.getWorkFile().getAbsolutePath() + "/openpose");
        if (!workOpenpose.exists()) {
            workOpenpose.mkdirs();
        }

        //openpose输入路径
        workOpenposeInput = new File(workOpenpose.getAbsolutePath() + "/input");
        if (!workOpenposeInput.exists()) {
            workOpenposeInput.mkdirs();
        }

        //openpose输出路径
        workOpenposeOutput = new File(workOpenpose.getAbsolutePath() + "/output");
        if (!workOpenposeOutput.exists()) {
            workOpenposeOutput.mkdirs();
        }

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


            //截图到openpose
            this.captureToOpenpose();

            //启动线程池持续获取视频流图片
            this.executeVideoCapture();



        }


        private void captureToOpenpose() {
            UpdateCaptureAndOpenpose updateCaptureAndOpenpose = applicationContext.getBean(UpdateCaptureAndOpenpose.class);
            updateCaptureAndOpenpose.setWorkCapture(workCapture);
            updateCaptureAndOpenpose.setWorkOpenposeInput(workOpenposeInput);
            updateCaptureAndOpenpose.setWorkOpenposeOutput(workOpenposeOutput);

            //启动同步openpose的线程
            new Timer().schedule(updateCaptureAndOpenpose, 1000, 1000);
        }


        @SneakyThrows
        private void executeVideoCapture() {
            final ExecutorService taskPool = Executors.newFixedThreadPool(videoConf.getItems().length);
            final CountDownLatch countDownLatch = new CountDownLatch(videoConf.getItems().length);

            //执行任务
            Arrays.stream(videoConf.getItems()).forEach((item) -> {
                taskPool.execute(() -> {
                    try {
                        applicationContext.getBean(TaskCore.class).run(workCapture, workOpenposeInput, item);
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
