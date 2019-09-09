package me.exrates.externalservice.services.impl;

import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.services.LongPoolingEventService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
@Service
public class LongPoolingEventServiceImpl implements LongPoolingEventService {

    private final BlockingQueue<String> bufferQueue = new LinkedBlockingDeque<>();

    @Override
    public String getResult() {
        StringBuilder result = new StringBuilder(StringUtils.EMPTY);

        while (!bufferQueue.isEmpty()) {
            try {
                result.append(bufferQueue.take());
                result.append("\n");
            } catch (InterruptedException ex) {
                log.error("Interrupted exception occurred");
            }
        }
        return result.toString();
    }

    @Override
    public BlockingQueue<String> getBufferQueue() {
        return bufferQueue;
    }
}