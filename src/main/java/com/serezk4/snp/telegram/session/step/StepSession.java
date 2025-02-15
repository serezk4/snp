package com.serezk4.snp.telegram.session.step;

import com.serezk4.snp.database.model.User;
import com.serezk4.snp.telegram.bot.Client;
import com.serezk4.snp.telegram.bot.UpdateProxy;
import com.serezk4.snp.telegram.command.Command;
import com.serezk4.snp.telegram.session.Session;
import com.serezk4.snp.telegram.session.SessionContext;
import com.serezk4.snp.telegram.session.manager.StepManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@Accessors(chain = true)
public final class StepSession extends Session {
    public static final String EXIT_SESSION = "Отмена";

    Deque<Step> steps;
    Command<StepSession> command;

    @NonFinal
    String currentValidationPattern = ".*";
    @NonFinal
    String currentValidationErrorText = "Неверный формат ввода. Попробуйте еще раз.";
    @NonFinal
    Step lastStep = null;
    @NonFinal
    boolean requirePhoto = false;

    public StepSession(List<Step> initialSteps, Command<StepSession> command, long chatId) {
        super(SessionContext.builder().chatId(chatId).build());

        this.steps = new ArrayDeque<>() {{
            addAll(initialSteps);
        }};
        this.command = command;
    }

    @Override
    public void next(Client client, UpdateProxy update, User user) {
        recordUserInput(update);

        if (update.getText() != null && isExitCommand(update.getText())) {
            sendExitMessage(client, update);
            destroy(client, update);
            return;
        }

        if (steps.isEmpty()) {
            destroy(client, update);
            command.execute(update, getSessionContext(), user);
            return;
        }

        processNextStep(client, update, user);
    }

    @Override
    public void destroy(Client client, UpdateProxy update) {
        if (!isSaveBotsMessages()) deleteBotMessages(client, update);
        if (!isSaveUsersMessages()) deleteUserMessages(client, update);
        StepManager.getInstance().destroySession(update.getChatId());
    }

    private boolean isExitCommand(String message) {
        return message.equalsIgnoreCase(EXIT_SESSION) || message.contains("cancel");
    }

    private void recordUserInput(UpdateProxy update) {
        if (!update.getSelf().hasCallbackQuery()) getUsersMessagesIds().add(update.getMessageId());
        getSessionContext().getMessages().add(update.getSelf());
    }

    private void sendExitMessage(Client bot, UpdateProxy update) {
        bot.executeAsync(SendMessage.builder()
                        .chatId(update.getChatId())
                        .text("<b>Закрыто</b>")
                        .parseMode(ParseMode.HTML)
                        .build())
                .thenAccept(message -> {
                    getBotsMessagesIds().add(message.getMessageId());
                    cleanupOldBotMessages(bot, update);
                });
    }

    private void processNextStep(Client bot, UpdateProxy update, User user) {
        Step.Data data = steps.peek().getGenerator().apply(update, user, getSessionContext());

        if (requirePhoto && !update.getSelf().getMessage().hasPhoto()) {
            data = Optional.ofNullable(lastStep)
                    .map(step -> step.getGenerator().apply(update, user, getSessionContext())).orElse(data);
            data.setText(data.getText().concat("\n\n<i>Необходимо прикрепить фото</i>"));
            log.info("Validation failed! Expected photo, received: {}", update.getText());
            getSessionContext().getMessages().removeLast();
        } else if (!requirePhoto && (update.getText() == null || !update.getText().matches(currentValidationPattern))) {
            data = Optional.ofNullable(lastStep)
                    .map(step -> step.getGenerator().apply(update, user, getSessionContext())).orElse(data);
            data.setText(data.getText().concat("\n\n<i>%s</i>".formatted(currentValidationErrorText)));
            log.info("Validation failed! Expected: {}, received: {}",
                    currentValidationPattern, update.getText());
            getSessionContext().getMessages().removeLast();
        } else {
            log.info("validation passed! {} passed pattern {}", data.getInputPattern(), update.getText());

            this.currentValidationPattern = data.getInputPattern();
            this.currentValidationErrorText = data.getErrorMessage();
            this.requirePhoto = data.isRequirePhoto();

            log.info("%set validationPatter to {} to session {}", this.currentValidationPattern, getId());
            log.info("%set validationErrorText to {} to session {}", this.currentValidationErrorText, getId());
            log.info("%set requirePhoto to {} to session {}", this.requirePhoto, getId());

            lastStep = steps.pop();
        }

        sendOrUpdateMessage(bot, update, data, user);
    }

    private void sendOrUpdateMessage(Client bot, UpdateProxy update, Step.Data data, User user) {
        ReplyKeyboard replyKeyboard = data.transferButtons();
        String text = data.getText();

        if (!getBotsMessagesIds().isEmpty() && replyKeyboard == null) {
            try {
                editMessage(bot, update, text);
                if (!isSaveUsersMessages()) deleteUserMessages(bot, update);
                return;
            } catch (Exception ignored) {
                log.info("Failed to edit message, sending new message");
            }
        }

        try {
            bot.executeAsync(SendMessage.builder()
                            .chatId(update.getChatId())
                            .text(text).parseMode(ParseMode.HTML)
                            .replyMarkup(replyKeyboard)
                            .build())
                    .thenAccept(message -> {
                        getBotsMessagesIds().add(message.getMessageId());
                        cleanupOldBotMessages(bot, update);
                        deleteUserMessages(bot, update);
                    }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send message", e);
        }
    }

    private void editMessage(Client client, UpdateProxy update, String text) throws Exception {
        client.executeWithException(EditMessageText.builder()
                .chatId(update.getChatId()).messageId(getBotsMessagesIds().getLast())
                .text(text).parseMode(ParseMode.HTML)
                .build());
    }

    private void cleanupOldBotMessages(Client client, UpdateProxy update) {
        if (isSaveBotsMessages()) return;
        log.info(getBotsMessagesIds());
        while (getBotsMessagesIds().size() > 1) {
            log.info("Deletion message with id {}", getBotsMessagesIds().getFirst());
            client.executeAsync(DeleteMessage.builder()
                    .chatId(update.getChatId())
                    .messageId(getBotsMessagesIds().removeFirst())
                    .build());
        }
    }

    private void deleteBotMessages(Client client, UpdateProxy update) {
        if (isSaveBotsMessages()) return;

        Deque<Integer> botsMessagesIds = getBotsMessagesIds();
        while (!botsMessagesIds.isEmpty()) {
            Integer msgId = botsMessagesIds.removeFirst();
            log.info("Deleting bot message with id {}", msgId);
            client.executeAsync(DeleteMessage.builder()
                            .chatId(update.getChatId()).messageId(msgId)
                            .build())
                    .thenAccept(_ -> log.info("Deleted bot message with id {}", msgId));
        }
    }

    private void deleteUserMessages(Client client, UpdateProxy update) {
        if (isSaveUsersMessages()) return;

        Deque<Integer> usersMessagesIds = getUsersMessagesIds();
        while (!usersMessagesIds.isEmpty()) {
            Integer msgId = usersMessagesIds.removeFirst();
            try {
                client.executeAsync(DeleteMessage.builder()
                        .chatId(update.getChatId()).messageId(msgId)
                        .build());
            } catch (Exception e) {
                log.error("Failed to delete user message with id {}", msgId, e);
            }
        }
    }
}
