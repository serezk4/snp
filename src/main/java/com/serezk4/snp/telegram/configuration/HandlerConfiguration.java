package com.serezk4.snp.telegram.configuration;

import com.serezk4.snp.database.service.UserService;
import com.serezk4.snp.telegram.bot.Client;
import com.serezk4.snp.telegram.bot.Handler;
import com.serezk4.snp.telegram.command.Command;
import com.serezk4.snp.telegram.session.Session;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Log4j2
public class HandlerConfiguration {
    @Bean
    public Handler tHandler(
            final UserService userService,
            final Client client,
            final List<Command<? extends Session>> commands
    ) {
        commands.forEach(command -> log.info("registered command: {}", command.getClass().getSimpleName()));
        return new Handler(commands, userService, client);
    }
}
