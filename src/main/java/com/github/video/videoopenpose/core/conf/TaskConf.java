package com.github.video.videoopenpose.core.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties("task")
public class TaskConf {

    //并发任务池数量
    private int taskPoolCount = 10;


    //每次执行休眠时长
    private long taskSleepTime = 3000;


    //工作目录 c:/workSpace
    private File workFile = new File(String.format("%s/workSpace/", System.getenv("SystemDrive")));

}
