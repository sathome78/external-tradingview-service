package me.exrates.externalservice.services;

import java.util.concurrent.BlockingQueue;

public interface LongPoolingEventService {

    String getResult();

    BlockingQueue<String> getBufferQueue();
}