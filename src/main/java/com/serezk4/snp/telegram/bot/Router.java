package com.serezk4.snp.telegram.bot;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Component
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Router {
    List<ExecutorService> services;
    int size;

    public Router(@Value("${telegram.bot.threads}") int size) {
        this.services = new ArrayList<>(size);
        this.size = size;

        IntStream.range(0, size)
                .mapToObj(_ -> Executors.newSingleThreadExecutor())
                .forEach(this.services::add);

        log.info("created executor service router with {} services", this.size);
    }

    public boolean isShutdown() {
        return services.stream().allMatch(ExecutorService::isShutdown);
    }

    public void push(long id, Runnable runnable) {
        if (isShutdown()) throw new IllegalStateException("cannot route task: all executors are shut down");

        final int executorId = (int) ((id) % size);
        log.info("routing task#{}... to executor {}", id, executorId);
        services.get(executorId).execute(runnable);
        log.info("sent task#{} to executor#{}", id, executorId);
    }

    public void shutdown() {
        log.info("shutting down...");
        services.forEach(ExecutorService::shutdown);
        log.info("turned off successfully");
    }
}
