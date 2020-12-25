package com.a99f.pack.utils;

/**
 * Copyright (C) 2020 A99F.COM Inc. All rights reserved.
 * This is source code from a99f-channel-apk-obfuscator.
 * The distribution of any copyright must be permitted by QiaoWeiRen Company.
 * 此代码归A99F(A99F.com)版权所有.
 * 概要说明:
 * 设计UI文档地址：
 * 产品文档地址：
 * 关联API地址 ：
 * 讨论文档地址：
 * 需求说明
 * 安全性说明：
 * 功能性说明：
 * 性能要求；
 * 输入参数：
 * 输出参数：
 * 数据库操作说明：
 * 日期: Created by liyu on 3:29 下午.
 * 作者: A99F
 * 更新版本          日期            作者             备注
 * v0001            2020/12/25     A99F            完成文件创建
 * 规划TODO-LIST：
 * 清单编号          预计日期         作者             状态               备注
 * td0001           0000/00/00      A99F          实现/未实现/进行中
 */
import java.io.*;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.tools.ant.filters.StringInputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
/**
 * Created by 83642 on 2017/8/9.
 */
public class ZipUtil {
    private static final int BUFFER = 1024;
    private static final String BASE_DIR = "";
    /**符号"/"用来作为目录标识判断符*/
    private static final String PATH = "/";

    /**
     * 解压缩zip文件
     * @param fileName 要解压的文件名 包含路径 如："c:\\test.zip"
     * @param filePath 解压后存放文件的路径 如："c:\\temp"
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public static void unZip(String fileName, String filePath) throws Exception{
        ZipFile zipFile = new ZipFile(fileName);
        Enumeration emu = zipFile.getEntries();

        while(emu.hasMoreElements()){
            ZipArchiveEntry entry = (ZipArchiveEntry) emu.nextElement();
            if (entry.isDirectory()){
                new File(filePath+entry.getName()).mkdirs();
                continue;
            }
            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

            File file = new File(filePath + entry.getName());
            File parent = file.getParentFile();
            if(parent != null && (!parent.exists())){
                parent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos,BUFFER);

            byte [] buf = new byte[BUFFER];
            int len = 0;
            while((len=bis.read(buf,0,BUFFER))!=-1){
                fos.write(buf,0,len);
            }
            bos.flush();
            bos.close();
            bis.close();
        }
        zipFile.close();
    }

    /**
     * 压缩文件
     *
     * @param srcFile
     * @param destPath
     * @throws Exception
     */
    public static void compress(String srcFile, String destPath) throws Exception {
        compress(new File(srcFile), new File(destPath));
    }

    /**
     * 压缩
     *
     * @param srcFile
     *            源路径
     *            目标路径
     * @throws Exception
     */
    public static void compress(File srcFile, File destFile) throws Exception {
        // 对输出文件做CRC32校验
        CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(
                destFile), new CRC32());

        ZipOutputStream zos = new ZipOutputStream(cos);
        compress(srcFile, zos, BASE_DIR);

        zos.flush();
        zos.close();
    }

    /**
     * 压缩
     *
     * @param srcFile
     *            源路径
     * @param zos
     *            ZipOutputStream
     * @param basePath
     *            压缩包内相对路径
     * @throws Exception
     */
    private static void compress(File srcFile, ZipOutputStream zos,
                                 String basePath) throws Exception {
        if (srcFile.isDirectory()) {
            compressDir(srcFile, zos, basePath);
        } else {
            compressFile(srcFile, zos, basePath);
        }
    }

    /**
     * 压缩目录
     *
     * @param dir
     * @param zos
     * @param basePath
     * @throws Exception
     */
    private static void compressDir(File dir, ZipOutputStream zos,
                                    String basePath) throws Exception {
        File[] files = dir.listFiles();
        // 构建空目录
        if (files.length < 1) {
            ZipEntry entry = new ZipEntry(basePath + dir.getName() + PATH);

            zos.putNextEntry(entry);
            zos.closeEntry();
        }

        String dirName = "";
        String path = "";
        for (File file : files) {
            //当父文件包名为空时，则不把包名添加至路径中（主要是解决压缩时会把父目录文件也打包进去）
            if(basePath!=null && !"".equals(basePath)){
                dirName=dir.getName();
            }
            path = basePath + dirName + PATH;
            // 递归压缩
            compress(file, zos, path);
        }
    }

    /**
     * 文件压缩
     *
     * @param file
     *            待压缩文件
     * @param zos
     *            ZipOutputStream
     * @param dir
     *            压缩文件中的当前路径
     * @throws Exception
     */
    private static void compressFile(File file, ZipOutputStream zos, String dir)
            throws Exception {
        /**
         * 压缩包内文件名定义
         *
         * <pre>
         * 如果有多级目录，那么这里就需要给出包含目录的文件名
         * 如果用WinRAR打开压缩包，中文名将显示为乱码
         * </pre>
         */
        if("/".equals(dir))dir="";
        else if(dir.startsWith("/"))dir=dir.substring(1,dir.length());

        ZipEntry entry = new ZipEntry(dir + file.getName());
        zos.putNextEntry(entry);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = bis.read(data, 0, BUFFER)) != -1) {
            zos.write(data, 0, count);
        }
        bis.close();

        zos.closeEntry();
    }
}

