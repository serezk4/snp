package com.serezk4.snp.telegram.session.step;

import com.serezk4.snp.database.model.User;
import com.serezk4.snp.telegram.bot.UpdateProxy;
import com.serezk4.snp.telegram.session.SessionContext;
import com.serezk4.snp.telegram.util.Keyboard;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class Step {
    Function<UpdateProxy, Data> generator;

    @FunctionalInterface
    public interface Function<update, data> {
        public Data apply(UpdateProxy update, User user, SessionContext context);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor
    @Builder
    @Getter
    public static class Data {
        @NonFinal
        @Setter
        String text;

        List<Button.Reply> replyButtons;
        List<Button.Inline> inlineButtons;

        @Builder.Default
        int rowSize = 2;
        boolean canGoNext = true;

        @Builder.Default
        String inputPattern = ".*";

        @Builder.Default
        String errorMessage = "Неверный формат ввода";

        @Builder.Default
        boolean requirePhoto = false;

        public ReplyKeyboard transferButtons() {
            if (replyButtons != null && !replyButtons.isEmpty()) return transferReplyButtons();
            if (inlineButtons != null && !inlineButtons.isEmpty()) return transferInlineButtons();
            return null;
        }

        private ReplyKeyboardMarkup transferReplyButtons() {
            if (replyButtons == null || replyButtons.isEmpty()) return null;

            return Keyboard.Reply.getResizableKeyboard(replyButtons.stream()
                    .map(button -> new Keyboard.Reply.Button(button.getText(), button.getWebAppInfo()))
                    .toList(), rowSize);
        }

        private InlineKeyboardMarkup transferInlineButtons() {
            if (inlineButtons == null || inlineButtons.isEmpty()) return null;

            return Keyboard.Inline.getResizableKeyboard(inlineButtons.stream()
                    .map(button -> new Keyboard.Inline.Button(button.getText(), button.getCallback(), button.getLink()))
                    .toList(), rowSize);
        }
    }

    public static class Button {
        private Button() {
        }

        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        @AllArgsConstructor
        @Getter
        public static class Inline extends Button {
            String text;
            String callback;
            String link;

            public Inline(String text, String callback) {
                this.text = text;
                this.callback = callback;
                this.link = null;
            }
        }

        @AllArgsConstructor
        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        @Getter
        public static class Reply extends Button {
            String text;
            WebAppInfo webAppInfo;

            public Reply(String text) {
                this.text = text;
                this.webAppInfo = null;
            }
        }
    }
}
