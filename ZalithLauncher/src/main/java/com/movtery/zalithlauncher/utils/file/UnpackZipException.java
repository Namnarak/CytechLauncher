package com.movtery.zalithlauncher.utils.file;

/**
 * 解压、读取 Zip 文件时遇到异常
 */
public class UnpackZipException extends RuntimeException {
    public UnpackZipException() {
        super();
    }

    public UnpackZipException(String message) {
        super(message);
    }
}
