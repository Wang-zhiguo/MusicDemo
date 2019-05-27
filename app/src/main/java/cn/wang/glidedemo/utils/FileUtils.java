package cn.wang.glidedemo.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Time: 2019-05-25
 * Author: wangzhiguo
 * Description: 功能描述
 */
public class FileUtils {
    private static String SDPATH = "";

    /**
     * 获取到sd卡的根目录，并以String形式返回
     *
     * @return
     */
    public static String getSDCardPath() {
        SDPATH = Environment.getExternalStorageDirectory() + "/";
        return SDPATH;
    }

    /**
     * 创建文件或文件夹
     *
     * @param fileName
     *            文件名或问文件夹名
     */
    public static void createFile(String fileName) {
        File file = new File(getSDCardPath() + fileName);
        if (fileName.indexOf(".") != -1) {
            // 说明包含，即是创建文件, 返回值为-1就说明不包含.,即文件夹
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 创建文件夹
            file.mkdir();
        }

    }
}
