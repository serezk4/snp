package com.serezk4.snp.telegram.command;

import com.serezk4.snp.database.model.User;
import com.serezk4.snp.telegram.bot.UpdateProxy;
import com.serezk4.snp.telegram.session.Session;
import com.serezk4.snp.telegram.session.SessionContext;
import com.serezk4.snp.telegram.session.empty.EmptySession;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Getter
public abstract class Command<T extends Session> {
    List<String> usage;
    String help;
    User.Role requiredRole;

    public Command(final List<String> usage, User.Role requiredRole) {
        this(usage, "[x]", requiredRole);
    }

    public Command(final List<String> usage) {
        this(usage, User.Role.MAX);
    }

    public abstract void execute(
            final UpdateProxy update,
            final SessionContext sessionContext,
            final User user
    );

    public Session createSession(final Long chatId) {
        return new EmptySession<>(this, chatId);
    }
}
