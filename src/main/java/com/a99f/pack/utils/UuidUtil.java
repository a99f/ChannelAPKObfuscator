package com.a99f.pack.utils;

import java.util.UUID;

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
public class UuidUtil {
    /**
     * 获得指定数目的UUID
     *
     * @param number int 需要获得的UUID数量
     * @return String[] UUID数组
     */
    public static String[] getUUID(int number) {
        if (number < 1) {
            return null;
        }
        String[] retArray = new String[number];
        for (int i = 0; i < number; i++) {
            retArray[i] = getUUID();
        }
        return retArray;
    }

    /**
     * 获得一个UUID
     *
     * @return String UUID
     */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        //去掉“-”符号
        return uuid.replaceAll("-", "");
    }
}

