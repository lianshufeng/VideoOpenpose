package com.github.video.videoopenpose.core.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "ffmpeg")
public class FFMPEGConf {

    private String path;


    private static File ffmpegPath = null;


    /**
     * 转换到ffmpeg
     *
     * @return
     */
    public File toFFMPEGFile() {
        if (ffmpegPath == null) {
            FileUtils.listFiles(new File(path), new String[]{"exe"}, true).stream().filter((file) -> {
                return "ffmpeg.exe".equalsIgnoreCase(file.getName());
            }).findFirst().ifPresent((file) -> {
                ffmpegPath = file;
            });
        }
        return ffmpegPath;
    }


}
