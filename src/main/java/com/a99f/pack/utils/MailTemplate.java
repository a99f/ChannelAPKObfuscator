package com.a99f.pack.utils;

import java.util.Arrays;
import java.util.List;


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
 * 日期: Created by liyu on 1:08 下午.
 * 作者: A99F
 * 更新版本          日期            作者             备注
 * v0001            2020/12/21     A99F            完成文件创建
 * 规划TODO-LIST：
 * 清单编号          预计日期         作者             状态               备注
 * td0001           0000/00/00      A99F          实现/未实现/进行中
 */
public class MailTemplate {
    /**
     * 发送邮件操作
     *
     * @param urlVersion   当前APK版本号
     * @param qudaoNames   渠道列表数组
     * @param putFileName  文件在容器内名称
     * @param completeTime 完成时间
     * @param bucketDomain 容器域名称
     * @throws Exception
     */
    public void sendMail(String urlVersion, String[] qudaoNames,
                         String putFileName, String completeTime, String bucketDomain) throws Exception {
        String userName = ReadINI.getIniData("mail", "mailUserName"); // 发件人邮箱
        String password = ReadINI.getIniData("mail", "mailPassword"); // 发件人密码
        String smtpHost = ReadINI.getIniData("mail", "mailSmtp"); // 邮件服务器

        String to = ReadINI.getIniData("mail", "sendUsers"); // 收件人，多个收件人以半角逗号分隔

        String cc = ReadINI.getIniData("mail", "carbonCopyUsers"); // 抄送，多个抄送以半角逗号分隔
        String subject = ReadINI.getIniData("mail", "mailSubject") + " V" + urlVersion; // 主题
        StringBuilder sb = new StringBuilder();
        sb.append("<table style='border:1px solid #ccc;'>");
        sb.append("<thead><tr><th>#</th><th style='width:30%'>名称</th><th style='width:40%'>链接</th></tr></thead>");
        sb.append("<tbody>");

        for (int i = 0; i < qudaoNames.length; i++) {
            String quDaoCNName = "";
            switch (qudaoNames[i]) {
                case "offical":
                    quDaoCNName = "官网";
                    break;
                case "douyin":
                    quDaoCNName = "抖音";
                    break;
                case "toutiao":
                    quDaoCNName = "头条搜索";
                    break;
                case "huoshan":
                    quDaoCNName = "火山视频";
                    break;
                case "tencent":
                    quDaoCNName = "腾讯应用宝";
                    break;
                case "huawei":
                    quDaoCNName = "华为";
                    break;
                case "meizu":
                    quDaoCNName = "魅族";
                    break;
                case "oppo":
                    quDaoCNName = "OPPO";
                    break;
                case "vivo":
                    quDaoCNName = "VIVO";
                    break;
                case "xiaomi":
                    quDaoCNName = "小米";
                    break;
                case "wandoujia":
                    quDaoCNName = "豌豆荚";
                    break;
                case "360":
                    quDaoCNName = "360";
                    break;
                case "baidu-search":
                    quDaoCNName = "百度搜索";
                    break;
                case "baidu-assist":
                    quDaoCNName = "百度助手";
                    break;
                default:
                    quDaoCNName = "未知";
                    break;
            }
            String cloudPath = bucketDomain + putFileName + "_" + qudaoNames[i] + "_jiagu_signed.apk";
            sb.append("<tr style='border:1px solid #ccc'><td>" + (i + 1) + "<td><td>" + quDaoCNName + "</td><td>" + cloudPath + "</td></tr>");
        }
        sb.append("</tbody>");
        sb.append("</table>");

        String body = "加固服务完成。<br/>文件名：" + putFileName +
                "，<br/>渠道" + Arrays.toString(qudaoNames) + ",<br/>加固执行时间：" + completeTime + "ms"
                + "<br/>渠道列表如下：" + sb.toString(); // 正文，可以用html格式的哟


        List<String> attachments = null;
        Email email = Email.entity(smtpHost, userName, password, to, cc, subject, body, attachments);
        email.send(); // 发送！
    }
}
