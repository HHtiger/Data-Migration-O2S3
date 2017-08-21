package com.tiger.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by root on 17-3-9.
 */
public class FinishedFileCounterTest {

    private static Logger logger = LoggerFactory.getLogger(FinishedFileCounterTest.class);

    @BeforeClass
    public static void beforeClass() throws FileNotFoundException {
        FinishedFileCounter.startCount();
    }

    @Test
    public void testSet() throws IOException {
        FinishedFileCounter.set(0);
        logger.warn(" i : {} ", FinishedFileCounter.get());
    }

    @Test
    public void testGet() throws IOException, InterruptedException {
        while (true) {
            logger.warn(" i : {} ", FinishedFileCounter.get());
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Test
    public void testAddAndGet() throws IOException {
        logger.warn(" i : {} ", FinishedFileCounter.addAndGet(1));
    }

    @AfterClass
    public static void afterClass() throws IOException {
        FinishedFileCounter.endCount();
    }

}