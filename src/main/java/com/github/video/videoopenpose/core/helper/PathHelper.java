package com.github.video.videoopenpose.core.helper;


import com.github.video.videoopenpose.core.conf.FFmpegConf;
import com.github.video.videoopenpose.core.conf.OpenposeConf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PathHelper {

    @Autowired
    private FFmpegConf ffmpegConf;

    @Autowired
    private OpenposeConf openposeConf;


    @Getter
    private File ffmpegFile = null;

    @Getter
    private File openposeHome = null;


    @Autowired
    private void initFFmpeg(ApplicationContext applicationContext) {
        String[] fileNames = new String[]{"ffmpeg.exe", "ffmpeg.bat", "ffmpeg"};
        AtomicReference<File> fileAtomicReference = new AtomicReference<>();
        Optional.ofNullable(ffmpegConf.getPath()).ifPresentOrElse((it) -> {
            fileAtomicReference.set(findFileName(new File(it), true, fileNames));
        }, () -> {
            fileAtomicReference.set(findFileFromSystemEnv(fileNames));
        });
        Optional.ofNullable(fileAtomicReference.get()).ifPresent((it) -> {
            this.ffmpegFile = it;
        });
        log.info("ffmpeg : {}", ffmpegFile);
    }


    @Autowired
    private void initOpenposeHome(ApplicationContext applicationContext) {
        String[] fileNames = new String[]{"OpenPoseDemo.exe", "OpenPose.exe", "OpenPose"};
        AtomicReference<File> fileAtomicReference = new AtomicReference<>();
        Optional.ofNullable(openposeConf.getPath()).ifPresentOrElse((it) -> {
            fileAtomicReference.set(findFileName(new File(it), true, fileNames));
        }, () -> {
            fileAtomicReference.set(findFileFromSystemEnv(fileNames));
        });
        Optional.ofNullable(fileAtomicReference.get()).ifPresent((it) -> {
            this.openposeHome = it.getParentFile().getParentFile();
        });
        log.info("openposeHome : {}", this.openposeHome);
    }


    /**
     * 通过系统环境变量查找文件
     *
     * @param fileNames
     * @return
     */
    private File findFileFromSystemEnv(String... fileNames) {
        List<File> pathFile = new ArrayList<>();
        Arrays.stream(System.getenv().get("Path").split(";")).forEach((paths) -> {
            pathFile.addAll(List.of(paths).stream().map(it -> new File(it)).collect(Collectors.toList()));
        });

        final AtomicReference<File> fileAtomicReference = new AtomicReference<>();
        pathFile.stream()
                .map(path -> findFileName(path, false, fileNames))
                .filter(it -> it != null)
                .findFirst()
                .ifPresent((it) -> {
                    fileAtomicReference.set(it);
                });
        return fileAtomicReference.get();
    }


    /**
     * 查询文件名
     *
     * @param path
     * @param fileNames
     * @return
     */
    private File findFileName(File path, boolean isFindSubFile, String... fileNames) {
        final Set<String> fileNameSet = Set.of(fileNames);
        final AtomicReference<File> fileAtomicReference = new AtomicReference<>();
        if (!path.isDirectory()) {
            return null;
        }

        Collection<File> files = null;
        if (isFindSubFile) {
            files = FileUtils.listFiles(path, new IOFileFilter() {
                @Override
                public boolean accept(File file) {
                    return true;
                }

                @Override
                public boolean accept(File dir, String name) {
                    return true;
                }
            }, new IOFileFilter() {
                @Override
                public boolean accept(File file) {
                    return true;
                }

                @Override
                public boolean accept(File dir, String name) {
                    return true;
                }
            });
        } else {
            files = List.of(path.listFiles());
        }


        files.stream()
                .filter(file -> file.isFile())
                .filter(file -> {
                    for (String fileName : fileNameSet) {
                        if (fileName.equalsIgnoreCase(file.getName())) {
                            return true;
                        }
                    }
                    return false;
                })
                .findFirst()
                .ifPresent((file) -> {
                    fileAtomicReference.set(file);
                });
        return fileAtomicReference.get();
    }


}
