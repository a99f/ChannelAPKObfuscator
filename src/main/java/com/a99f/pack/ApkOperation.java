package com.a99f.pack;


import com.a99f.pack.utils.DownloadUtil;
import com.a99f.pack.utils.MailTemplate;
import com.a99f.pack.utils.UuidUtil;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.dzm.apkqudao.ZipUtil.unZip;
import static com.a99f.pack.utils.ReadINI.readIni;

public class ApkOperation {
    //加固登录名
    private static String JIAGU_LOGIN_NAME = "";
    //加固密码
    private static String JIAGU_LOGIN_PASSWORD = "";
    //密钥存储路径
    private static String STORE_PATH = "";
    //密钥密码
    private static String STORE_PASSWORD = "";
    //密钥KEY别名
    private static String KEY_ALIAS = "";
    //密钥别名密码
    private static String KEY_ALIAS_PASSWORD = "";
    //远程APK地址
    private static String REMOTE_APK_URL_PATH = "";
    //APK加固后版本，360会自动识别到APP内的版本，所以这里必须一致
    private static String APK_VERSION = "";
    //放到云端的命名
    private static String APK_CLOUD_TARGET_NAME = "";
    //本地目标文件路径
    private static String LOCAL_DEST_FILE_PATH = "";
    //本地目标文件名称
    private static String LOCAL_DEST_FILE_NAME = "";
    //七牛容器绑定域名
    private static String BUCKET_DOMAIN_URL = "";
    //加固渠道名称组
    private static String QUDAO_NAMES_STRING = "";


    /**
     * 执行APK操作
     */
    public static void doAPKOperation() {
        Map<String, Object> ini = readIni("resource/config/config.ini");
        //加固登录名
        JIAGU_LOGIN_NAME = getIniData(ini, "jiagu", "LOGIN_NAME");
        //加固密码
        JIAGU_LOGIN_PASSWORD = getIniData(ini, "jiagu", "LOGIN_PASSWORD");
        //密钥存储路径
        STORE_PATH = getIniData(ini, "keystore", "STORE_PATH");
        //密钥密码
        STORE_PASSWORD = getIniData(ini, "keystore", "STORE_PASSWORD");
        //密钥KEY别名
        KEY_ALIAS = getIniData(ini, "keystore", "KEY_ALIAS");
        //密钥别名密码
        KEY_ALIAS_PASSWORD = getIniData(ini, "keystore", "KEY_ALIAS_PASSWORD");
        //远程APK地址
        REMOTE_APK_URL_PATH = getIniData(ini, "apk", "url");
        //APK加固后版本，360会自动识别到APP内的版本，所以这里必须一致
        APK_VERSION = getIniData(ini, "apk", "version");
        //放到云端的命名
        APK_CLOUD_TARGET_NAME = getIniData(ini, "apk", "cloudTargetName");
        //本地目标文件路径
        LOCAL_DEST_FILE_PATH = getIniData(ini, "apk", "destFilePath");
        //本地目标文件名称
        LOCAL_DEST_FILE_NAME = getIniData(ini, "apk", "destFileName");
        //七牛容器绑定域名
        BUCKET_DOMAIN_URL = getIniData(ini, "qiniu", "BUCKET_DOMAIN_URL");
        //七牛容器绑定域名
        BUCKET_DOMAIN_URL = getIniData(ini, "qiniu", "BUCKET_DOMAIN_URL");
        //加固渠道名称组
        QUDAO_NAMES_STRING = getIniData(ini, "qudao", "qudaoNames");

        //运行之前清空APK文件夹
        ZipApk.delFile(new File(LOCAL_DEST_FILE_PATH));

        //判断创建APK文件夹
        File targetFile = new File(LOCAL_DEST_FILE_PATH);
        if (!targetFile.exists()) {
            targetFile.mkdir();
        }

        //启动时间
        final long startTime = new Date().getTime();

        //下载并监听
        new DownloadUtil().download(REMOTE_APK_URL_PATH, LOCAL_DEST_FILE_PATH, LOCAL_DEST_FILE_NAME, new DownloadUtil.OnDownloadListener() {
            //下载成功
            // @Override
            public void onDownloadSuccess(File file) throws Exception {
                System.out.println("download file success");
                //结束时间
                long endTime = new Date().getTime();
                System.out.println("downloadTime:" + (endTime - startTime) + "ms");
                //执行加固操作
                doAPKEncryptAndJiaGu(APK_VERSION, APK_CLOUD_TARGET_NAME);
            }

            //下载中
            // @Override
            public void onDownloading(int progress) {
                System.out.println("DOWNLOADING..." + progress + "%");
            }

            //下载错误
            // @Override
            public void onDownloadFailed(Exception e) {
                System.out.println("download file failed");
                long endTime = new Date().getTime();

                System.out.println("downloadTime:" + (endTime - startTime) + "ms");
            }
        });
    }


    /**
     * APK混淆并加固
     *
     * @param urlVersion
     * @param putFileName
     * @throws Exception
     */
    private static void doAPKEncryptAndJiaGu(String urlVersion, String putFileName) throws Exception {
        String filePath = LOCAL_DEST_FILE_PATH + "/" + LOCAL_DEST_FILE_NAME;
        String fileName = "offical";

        System.out.println("isExist:" + ZipApk.isResourceExist(filePath));
        //生成随机文件名称
        String randomStr = UuidUtil.getUUID().substring(0, 8);
        //解压文件路径
        String unzipPath = "resource/apk/" + fileName + "_" + randomStr + "_unzip/";
        //执行解压文件操作
        unZip(filePath, unzipPath);
        //APK包内META信息路径
        String unzipMETAPath = unzipPath + "META-INF";
        //删除签名文件
        ZipApk.deleteMETAFiles(unzipMETAPath);

        //渠道名称组
        String[] qudaoNames = QUDAO_NAMES_STRING.split(",");

        //创建签名文件夹
        ZipApk.makePackageFiles();
        //整合渠道文件
        ZipApk.makeQuDaoIntoAPK(qudaoNames, unzipPath, fileName);

        //删除解压文件夹
        File f = new File(unzipPath);
        ZipApk.delFile(f);

        //加固登录
        new jiaguOperation().doLogin(JIAGU_LOGIN_NAME, JIAGU_LOGIN_PASSWORD);
        //加固导入签名
        new jiaguOperation().doImportKeyStore(STORE_PATH, STORE_PASSWORD, KEY_ALIAS, KEY_ALIAS_PASSWORD);

        //线程锁
        final CountDownLatch latch = new CountDownLatch(qudaoNames.length);

        //加固开始时间
        long jiaguStartTime = new Date().getTime();

        //多线程加固
        for (int i = 0; i < qudaoNames.length; i++) {
            new Thread(new JiaGuThread(latch, qudaoNames[i], urlVersion, putFileName)).start();
        }

        try {
            System.out.println("等待" + qudaoNames.length + "个线程运行完毕...");
            latch.await();
            //加固结束时间
            long jiaguEndTime = new Date().getTime();

            System.out.println(qudaoNames.length + "个线程已运行完毕,执行时间:" + (jiaguEndTime - jiaguStartTime) + "ms");

            //发送邮件通知加固完成
            new MailTemplate().sendMail(urlVersion, qudaoNames, putFileName, (jiaguEndTime - jiaguStartTime) + "", BUCKET_DOMAIN_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ApkOperation apkOperation;


    public static ApkOperation get() {
        if (apkOperation == null) {
            apkOperation = new ApkOperation();
        }
        return apkOperation;
    }

    public ApkOperation() {

    }

    /**
     * 从INI数组中读取字段并返回
     *
     * @param iniData      Map数组
     * @param levelOneName 一级名称
     * @param levelTwoName 二级名称
     * @return String 二级键值
     */
    private static String getIniData(Map<String, Object> iniData, String levelOneName, String levelTwoName) {
        return ((Map<String, String>) iniData.get(levelOneName)).get(levelTwoName);
    }
}
