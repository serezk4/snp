package com.serezk4.snp.telegram.session.empty;

import com.serezk4.snp.database.model.User;
import com.serezk4.snp.telegram.bot.Client;
import com.serezk4.snp.telegram.bot.UpdateProxy;
import com.serezk4.snp.telegram.command.Command;
import com.serezk4.snp.telegram.session.Session;
import com.serezk4.snp.telegram.session.SessionContext;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmptySession<T extends Session> extends Session {
    Command<T> command;

    public EmptySession(Command<T> command, Long chatId) {
        super(SessionContext.builder().chatId(chatId).build());
        this.command = command;
    }

    @Override
    public void destroy(Client client, UpdateProxy update) {
        // empty session can be destroyed
    }

    @Override
    public void next(Client client, UpdateProxy update, User user) {
        command.execute(update, getSessionContext(), user);
    }
}
