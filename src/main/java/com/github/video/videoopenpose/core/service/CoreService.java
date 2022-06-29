package com.github.video.videoopenpose.core.service;

import com.github.video.videoopenpose.core.conf.TaskConf;
import com.github.video.videoopenpose.core.conf.VideoConf;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
    private void init(ApplicationContext applicationContext) {


        //执行任务
        executeTasks();

    }


    @SneakyThrows
    private void executeTasks() {
        final ExecutorService taskPool = Executors.newFixedThreadPool(taskConf.getTaskPoolCount());
        final CountDownLatch countDownLatch = new CountDownLatch(videoConf.getItems().length);
        //执行任务
        taskPool.execute(() -> {
            try {
                Arrays.stream(videoConf.getItems()).forEach((item) -> {
                    task(item);
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        taskPool.shutdownNow();
    }

    /**
     * 执行单个任务
     *
     * @param item
     */
    private void task(VideoConf.Item item) {
        System.out.println(item);
    }


}
