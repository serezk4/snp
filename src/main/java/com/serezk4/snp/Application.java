package com.serezk4.snp;

import com.serezk4.snp.telegram.bot.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@SpringBootApplication(scanBasePackages = "com.serezk4.snp")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Log4j2
public class Application implements ApplicationRunner {
    Bot bot;

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            log.info("starting bot...");
            botsApplication.registerBot(bot.getToken(), bot);
            log.info("bot started");
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("error occurred: ", e);
        }
    }
}
