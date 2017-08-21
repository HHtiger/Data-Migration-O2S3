package com.tiger.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 17-3-8.
 */
public class ShellUtill {

    private static Logger logger = LoggerFactory.getLogger("upload");

    public static int executeShell(String shellString) {
        int res = 0;
        List<String> shell = new ArrayList<>();
        shell.add("/bin/sh");
        shell.add("-c");
        shell.add(shellString);

        ProcessBuilder processBuilder = new ProcessBuilder(shell);
        processBuilder.redirectErrorStream(true);
        Process p;
        try {
            p = processBuilder.start();
            p.waitFor();
        } catch (IOException e) {
            logger.error("count file error:{}", e.getMessage());
            return res;
        } catch (InterruptedException e) {
            logger.error("count file error:{}", e.getMessage());
            return res;
        }
        InputStream is = p.getInputStream();
        BufferedReader bs = new BufferedReader(new InputStreamReader(is));

        if (p.exitValue() != 0) {
            //说明命令执行失败
            //可以进入到错误处理步骤中
        }
        String line;
        String resultLog = "";

        try {
            while ((line = bs.readLine()) != null) {
                resultLog += line;
            }
            res = Integer.valueOf(resultLog);
        } catch (IOException e) {
            logger.error("count file error:{}", e.getMessage());
            return res;
        }

        return res;

    }
}
