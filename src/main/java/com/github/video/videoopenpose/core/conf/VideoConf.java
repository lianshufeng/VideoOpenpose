package com.github.video.videoopenpose.core.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "video")
public class VideoConf {


    //视频列表
    private Item[] items;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private String name;
        private String url;
    }

}
