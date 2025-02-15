package com.serezk4.snp.telegram.session;

import com.serezk4.snp.database.model.User;
import com.serezk4.snp.telegram.bot.Client;
import com.serezk4.snp.telegram.bot.UpdateProxy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@Accessors(chain = true)
public abstract class Session {
    static AtomicLong idCounter = new AtomicLong(0L);

    SessionContext sessionContext;

    Deque<Integer> botsMessagesIds = new LinkedList<>();
    Deque<Integer> usersMessagesIds = new LinkedList<>();

    @Getter @Setter @NonFinal boolean saveUsersMessages = true;
    @Getter @Setter @NonFinal boolean saveBotsMessages = false;

    final long id = idCounter.getAndIncrement();

    public void next(
            final Client client,
            final UpdateProxy update,
            final User user
    ) {
    }

    public void destroy(
            final Client client,
            final UpdateProxy update
    ) {
    }
}
