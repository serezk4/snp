package com.serezk4.snp.telegram.session.manager;

import com.serezk4.snp.telegram.session.step.StepSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class StepManager {
    private static StepManager instance = null;
    private final Map<Long, Stack<StepSession>> stepSessions = new HashMap<>();

    private StepManager() {
    }

    public static StepManager getInstance() {
        return instance == null ? instance = new StepManager() : instance;
    }

    public synchronized boolean containsSession(long chatId) {
        return stepSessions.containsKey(chatId) && !stepSessions.get(chatId).isEmpty();
    }

    public synchronized void addSession(StepSession session, long chatId) {
        if (!stepSessions.containsKey(chatId)) stepSessions.put(chatId, new Stack<>());
        stepSessions.get(chatId).add(session);
    }

    public synchronized StepSession getSession(long chatId) {
        if (!stepSessions.containsKey(chatId) || stepSessions.get(chatId).isEmpty()) return null;
        return stepSessions.get(chatId).peek();
    }

    public synchronized StepSession destroySession(long chatId) {
        if (!stepSessions.containsKey(chatId)) return null;
        return stepSessions.get(chatId).pop();
    }
}
