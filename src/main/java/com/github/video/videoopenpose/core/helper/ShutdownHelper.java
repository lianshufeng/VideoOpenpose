package com.github.video.videoopenpose.core.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class ShutdownHelper {


    @Autowired
    private void init(ApplicationContext applicationContext) {
        final long pid = ProcessHandle.current().pid();
        //关闭子进程
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("close {} - {} ", "ffmpeg", "openposeDemo");
            try {
                Runtime.getRuntime().exec("cmd /c taskkill /f /t /pid " + String.valueOf(pid));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
