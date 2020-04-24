package com.xm.comment_utils.ip;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import org.aspectj.weaver.ast.Test;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;

public class IpUtil {
    private static String DB_PATH = null;
    static {
        DB_PATH = IpUtil.class.getResource("/ip/ip2region.db").getPath();
        File file = new File(DB_PATH);
        if (file.exists() == false) {
            String tmpDir = System.getProperties().getProperty("java.io.tmpdir");
            DB_PATH = tmpDir + "ip.db";
            System.out.println(DB_PATH);
            file = new File(DB_PATH);
            FileUtil.writeFromStream(IpUtil.class.getClassLoader().getResourceAsStream("classpath:ip/ip2region.db"),file);
        }
    }

    private static final int DEFAULT_SEARCH = DbSearcher.BTREE_ALGORITHM;

    public static String getCityInfo(String ip) {
        return getCityInfo(ip,DEFAULT_SEARCH);
    }
    public static String getCityInfo(String ip,int algorithm) {
        try {
            if(!Util.isIpAddress(ip))
                return null;
            DbConfig config = new DbConfig();
            DbSearcher searcher = new DbSearcher(config, DB_PATH);
            Method method = null;
            switch (algorithm) {
                case DbSearcher.BTREE_ALGORITHM:
                    method = searcher.getClass().getMethod("btreeSearch", String.class);
                    break;
                case DbSearcher.BINARY_ALGORITHM:
                    method = searcher.getClass().getMethod("binarySearch", String.class);
                    break;
                case DbSearcher.MEMORY_ALGORITYM:
                    method = searcher.getClass().getMethod("memorySearch", String.class);
                    break;
            }
            DataBlock dataBlock = null;
            if (Util.isIpAddress(ip) == false) {
                System.out.println("Error: Invalid ip address");
            }

            dataBlock = (DataBlock) method.invoke(searcher, ip);
            return dataBlock.getRegion();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(getCityInfo("127.0.0.1"));
    }
}
