package com.github.video.videoopenpose.core.task;

import com.github.video.videoopenpose.core.helper.PathHelper;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope("prototype")
public class UpdateCaptureAndOpenpose extends TimerTask {


    @Setter//工作空间
    private File workCapture;
    @Setter
    private File workOpenposeInput;

    @Setter
    private File workOpenposeOutput;

    @Autowired
    private PathHelper pathHelper;


    /**
     * 同步截图
     */
    private void updateCapture() {
        Arrays.stream(workCapture.listFiles()).filter(file -> file.exists() && file.isDirectory()).forEach((workImageFile) -> {
            AtomicInteger fileId = new AtomicInteger();
            Arrays.stream(workImageFile.listFiles()).map((it) -> {
                        return FilenameUtils.getBaseName(it.getName());
                    }).filter((it) -> {
                        return it != null;
                    }).map((it) -> {
                        return Integer.parseInt(it);
                    }).sorted((t1, t2) -> {
                        return t2 - t1;
                    }).findFirst()
                    .ifPresent((it) -> {
                        fileId.set(it);
                    });
            Optional.ofNullable(fileId.get()).ifPresent((it) -> {
                File source = new File(workImageFile.getAbsolutePath() + "/" + it + "." + TaskCore.ImageExtName);
                //图片名
                File target = new File(workOpenposeInput.getAbsolutePath() + "/" + workImageFile.getName() + "." + TaskCore.ImageExtName);
                if (source.exists()) {
                    copyFile(source, target);
                }
            });

            try {
                FileUtils.cleanDirectory(workImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @SneakyThrows
    private void openposeJob() {
        log.info("openpose - capture : {}", Arrays.stream(workOpenposeInput.listFiles()).map(it->it.getName()).collect(Collectors.toList()));
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(pathHelper.getOpenposeHome());
        processBuilder.command(new String[]{
                new File(pathHelper.getOpenposeHome() + "\\bin\\OpenPoseDemo.exe").getAbsolutePath(),
                "-display", "0",
                "-image_dir", workOpenposeInput.getAbsolutePath(),
                "-write_images", workOpenposeOutput.getAbsolutePath(),
                "-write_json", workOpenposeOutput.getAbsolutePath()
        });
        Process process = processBuilder.start();
        log.info("openpose - ret : {}", StreamUtils.copyToString(process.getInputStream(), Charset.forName("UTF-8")));

        process.waitFor();
    }

    @Override
    @SneakyThrows
    public void run() {

        //更新截图
        this.updateCapture();

        //执行openpose
        this.openposeJob();

    }


    @SneakyThrows
    private void copyFile(File source, File target) {
        log.info("capture : {}", source.getParentFile().getName());
        FileUtils.copyFile(source, target);
    }

}
