package com.serezk4.snp.telegram.bot;

import com.serezk4.snp.database.model.User;
import com.serezk4.snp.database.service.UserService;
import com.serezk4.snp.telegram.command.Command;
import com.serezk4.snp.telegram.session.Session;
import com.serezk4.snp.telegram.session.empty.EmptySession;
import com.serezk4.snp.telegram.session.manager.StepManager;
import com.serezk4.snp.telegram.session.step.StepSession;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Handler {
    @Getter
    List<Command<? extends Session>> commands;

    UserService userService;
    Client client;

    StepManager stepManager = StepManager.getInstance();

    public void process(final UpdateProxy update) {
        final long chatId = update.getChatId();
        final String username = update.getUsername();
        final String text = Optional.ofNullable(update.getText())
                .map(t -> new String(t.getBytes(), StandardCharsets.UTF_8))
                .orElse("");

        log.info("Chat ID: {}, Username: {}, Text: {}", chatId, username, text);

        log.info(chatId);
        log.info(userService.findByChatId(chatId));

        final User user = userService.findByChatId(chatId)
                .orElseGet(() -> userService.save(User.builder()
                        .chatId(chatId)
                        .username(username)
                        .utm(getUtm(text))
                        .build()));

        if (!user.isAccountNonLocked()) {
            client.sendMessage(chatId, "Ваш аккаунт заблокирован. Обратитесь к администратору.");
            return;
        }

        if (stepManager.containsSession(chatId)) {
            stepManager.getSession(chatId).next(client, update, user);
            return;
        }

        final Optional<Command<? extends Session>> optionalCommand = commands.stream()
                .filter(c -> c.getUsage().contains(text))
                .findFirst();

        if (optionalCommand.isEmpty()) {
            client.sendMessage(chatId, getHelp(user.getRole()));
            return;
        }

        if (optionalCommand.get().getRequiredRole().getLevel() > user.getRole().getLevel()) {
            client.sendMessage(chatId, "У вас недостаточно прав для выполнения этой команды.");
            return;
        }

        Session session = optionalCommand.get().createSession(chatId);

        if (session instanceof StepSession stepSession) stepManager.addSession(stepSession, chatId);

        try {
            session.next(client, update, user);
        } catch (Exception e) {
            log.error("Error while executing command: {}", e.getMessage());
            client.sendMessage(chatId, "Произошла ошибка при выполнении команды.");
        }
    }

    private String getUtm(final String text) {
        return text.contains(" ") ? text.substring(text.indexOf(' ') + 1) : "";
    }

    private String getHelp(User.Role role) {
        return "Кажется, вы ошиблись командой. Список всех команд:".concat(commands.stream()
                .filter(command -> command.getRequiredRole().getLevel() <= role.getLevel())
                .map(command -> String.join(" - ", command.getUsage().toString(), command.getHelp()))
                .reduce((a, b) -> String.join("\n", a, b))
                .orElse("Команды не найдены."));
    }
}
