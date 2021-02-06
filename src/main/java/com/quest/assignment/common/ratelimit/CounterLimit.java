package com.quest.assignment.common.ratelimit;

import java.util.concurrent.TimeUnit;

public abstract class CounterLimit {

    protected int limitCount;

    protected long limitTime;

    protected TimeUnit timeUnit;

    protected volatile boolean limited;

    protected abstract boolean tryCount();
}
