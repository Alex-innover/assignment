package com.quest.assignment.common.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;

public class SlidingWindowLimit extends CounterLimit {

    private static Logger logger = LoggerFactory.getLogger(SlidingWindowLimit.class);

    private AtomicInteger[] gridDistribution;

    private volatile int currentIndex;

    private int preTotalCount;

    private int gridNumber;

    private volatile boolean resetting;

    public SlidingWindowLimit(int gridNumber, int limitCount, long limitTime) {
        this(gridNumber, limitCount, limitTime, TimeUnit.SECONDS);
    }

    public SlidingWindowLimit(int gridNumber, int limitCount, long limitTime, TimeUnit timeUnit) {
        if (gridNumber <= limitTime)
            throw new RuntimeException("rate limit access error，gridNumber must be greater than limitTime");
        this.gridNumber = gridNumber;
        this.limitCount = limitCount;
        this.limitTime = limitTime;
        this.timeUnit = timeUnit;
        gridDistribution = new AtomicInteger[gridNumber];
        for (int i = 0; i < gridNumber; i++) {
            gridDistribution[i] = new AtomicInteger(0);
        }
        new Thread(new CounterResetThread()).start();
    }

    public boolean tryCount() {
        while (true) {
            if (limited) {
                return false;
            } else {
                int currentGridCount = gridDistribution[currentIndex].get();
                if (preTotalCount + currentGridCount == limitCount) {
                    logger.info("access limit over setting：{}", LocalDateTime.now().toString());
                    limited = true;
                    return false;
                }
                if (!resetting && gridDistribution[currentIndex].compareAndSet(currentGridCount, currentGridCount + 1))
                    return true;
            }
        }
    }

    class CounterResetThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    timeUnit.sleep(1);
                    int indexToReset = currentIndex - limitCount - 1;
                    if (indexToReset < 0) indexToReset += gridNumber;
                    resetting = true;
                    preTotalCount = preTotalCount - gridDistribution[indexToReset].get()
                            + gridDistribution[currentIndex++].get();
                    if (currentIndex == gridNumber) currentIndex = 0;
                    if (preTotalCount + gridDistribution[currentIndex].get() < limitCount)
                        limited = false;
                    resetting = false;
                    logger.info("Current Grid：{}，Reset Grid：{}，Rest Grid Access Amount：{}，Current Grid  Access Amount：{}",
                            currentIndex, indexToReset, gridDistribution[indexToReset].get(), preTotalCount);
                    gridDistribution[indexToReset].set(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
