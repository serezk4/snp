package com.serezk4.snp.telegram.bot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Bot implements LongPollingUpdateConsumer {
    Router router;
    Handler handler;
    @Getter String token;

    public Bot(
            final Router router,
            final Handler handler,
            final @Value("${telegram.bot.token}") String token
    ) {
        this.router = router;
        this.handler = handler;
        this.token = token;
    }

    @Override
    public void consume(List<Update> list) {
        list.stream()
                .map(UpdateProxy::new)
                .forEach(update -> router.push(update.getChatId(), () -> handler.process(update)));
    }
}
