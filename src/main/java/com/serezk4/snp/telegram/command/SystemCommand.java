package com.serezk4.snp.telegram.command;

import com.serezk4.snp.database.model.User;
import com.serezk4.snp.telegram.session.Session;

import java.util.List;

public abstract class SystemCommand<K extends Session> extends Command<K> {
    public SystemCommand(List<String> usage, String help) {
        super(usage, help, User.Role.MIN);
    }
}
