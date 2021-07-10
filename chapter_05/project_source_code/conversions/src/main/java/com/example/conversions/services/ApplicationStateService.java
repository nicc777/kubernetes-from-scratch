package com.example.conversions.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Scope("singleton")
@Slf4j
@Getter
public class ApplicationStateService {
    
    @Value("${service.ready.waittime:5000}")
    private Integer readyWaitTime;

    @Value("${service.terminate.waittime:5000}")
    private Integer terminateWaitTime;

    private boolean ready = false;

    public void prepareReadyState() throws InterruptedException {
        // Simulate startup tasks...
        if (!ready) {
            log.info("Starting up...");
            Thread.sleep(readyWaitTime);
            ready = true;
        }
        log.info("READY");
    }

    public void prepareForTermination() throws InterruptedException {
        // Simulate termination tasks...
        ready = false;
        log.info("Shutting down...");
        Thread.sleep(terminateWaitTime);
        log.info("TERMINATING");
    }

}
