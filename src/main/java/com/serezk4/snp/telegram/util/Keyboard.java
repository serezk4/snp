package com.serezk4.snp.telegram.util;

import com.serezk4.snp.database.model.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for creating various types of Telegram keyboards.
 * This class provides methods for generating both inline and reply keyboards with various customization options.
 * <p>
 * Key features:
 * - Inline and Reply keyboards generation.
 * - WebApp buttons support.
 * - Customizable layouts for buttons.
 * - Static keyboards for frequent use cases.
 */
@Log4j2
public class Keyboard {

    private Keyboard() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Utility constants for keyboard creation.
     */
    public static class Delimiter {
        public static final String SERVICE = "$";
        public static final String DATA = "*";
    }

    /**
     * Predefined actions available for keyboard buttons.
     */
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Getter
    @AllArgsConstructor
    public enum Actions {
        CLOSE("\uD83E\uDD0F Закрыть", "exit"),
        BACK("Назад", "back");

        String name;
        String callback;
    }

    /**
     * Class for generating reply keyboards.
     * These are the keyboards that users see under the text input field.
     */
    public static class Reply {
        public static final ReplyKeyboardMarkup DEFAULT = getDefault();

        /**
         * Generates the default reply keyboard with predefined buttons.
         *
         * @return {@link ReplyKeyboardMarkup} default reply keyboard.
         */
        public static ReplyKeyboardMarkup getDefault() {
            return getCustomKeyboard(new Button[][]{
                    {new Button("тестовая кнопка!")},
                    {new Button("крутая кнопка!")}
            });
        }

        public static ReplyKeyboardMarkup getDefault(User.Role role) {
            return getDefault();
        }

        /**
         * Generates a custom reply keyboard with text buttons provided as a 2D array.
         *
         * @param strings 2D array of button texts.
         * @return {@link ReplyKeyboardMarkup} reply keyboard with custom buttons.
         */
        private static ReplyKeyboardMarkup getCustomKeyboard(String[][] strings) {
            return getCustomKeyboard(Arrays.stream(strings)
                    .map(Arrays::asList)
                    .collect(Collectors.toList()));
        }

        /**
         * Generates a custom reply keyboard with text buttons provided as a 2D list.
         *
         * @param buttonsText 2D list of button texts.
         * @return {@link ReplyKeyboardMarkup} reply keyboard with custom buttons.
         */
        public static ReplyKeyboardMarkup getCustomKeyboard(List<List<String>> buttonsText) {
            return getCustomKeyboard(buttonsText, false);
        }

        /**
         * Generates a custom reply keyboard with Button objects.
         *
         * @param buttons 2D array of {@link Button} objects.
         * @return {@link ReplyKeyboardMarkup} reply keyboard with custom buttons.
         */
        public static ReplyKeyboardMarkup getCustomKeyboard(Button[][] buttons) {
            List<KeyboardRow> mainRow = Arrays.stream(buttons)
                    .map(row -> Arrays.stream(row)
                            .map(Reply::getButton)
                            .collect(Collectors.toCollection(KeyboardRow::new)))
                    .collect(Collectors.toList());

            return createReplyKeyboardMarkup(mainRow);
        }

        /**
         * Generates a customizable reply keyboard with an option to add default action buttons.
         *
         * @param buttonsText 2D list of button texts.
         * @param addButtons  whether to add default action buttons (BACK and CLOSE).
         * @return {@link ReplyKeyboardMarkup} customizable reply keyboard.
         */
        public static ReplyKeyboardMarkup getCustomKeyboard(List<List<String>> buttonsText, boolean addButtons) {
            if (addButtons) buttonsText.add(List.of(Actions.BACK.getName(), Actions.CLOSE.getName()));
            List<KeyboardRow> rows = buttonsText.stream()
                    .map(row -> row.stream()
                            .filter(Objects::nonNull)
                            .map(Reply::getButton)
                            .collect(Collectors.toCollection(KeyboardRow::new)))
                    .collect(Collectors.toList());

            return createReplyKeyboardMarkup(rows);
        }

        /**
         * Generates a reply keyboard with customizable row size.
         *
         * @param buttons list of {@link Button} objects.
         * @param rowSize number of buttons per row.
         * @return {@link ReplyKeyboardMarkup} resizable keyboard.
         */
        public static ReplyKeyboardMarkup getResizableKeyboard(List<Button> buttons, int rowSize) {
            List<KeyboardRow> mainRow = new ArrayList<>();
            Queue<Button> buttonsQueue = new LinkedList<>(buttons);

            while (!buttonsQueue.isEmpty()) {
                mainRow.add(IntStream.range(0, Math.min(rowSize, buttonsQueue.size()))
                        .mapToObj(_ -> buttonsQueue.poll())
                        .filter(Objects::nonNull)
                        .map(Reply::getButton)
                        .collect(Collectors.toCollection(KeyboardRow::new)));
            }

            return createReplyKeyboardMarkup(mainRow);
        }

        /**
         * Helper method to create {@link ReplyKeyboardMarkup} from rows of buttons.
         *
         * @param rows list of {@link KeyboardRow}.
         * @return {@link ReplyKeyboardMarkup} reply keyboard markup.
         */
        private static ReplyKeyboardMarkup createReplyKeyboardMarkup(List<KeyboardRow> rows) {
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(rows);
            replyKeyboardMarkup.setResizeKeyboard(true);
            return replyKeyboardMarkup;
        }

        /**
         * Converts {@link Button} object to {@link KeyboardButton}.
         *
         * @param button custom {@link Button} object.
         * @return {@link KeyboardButton} generated button.
         */
        private static KeyboardButton getButton(Button button) {
            return getButton(button.getText(), button.getWebAppInfo());
        }

        /**
         * Creates a {@link KeyboardButton} with given text.
         *
         * @param text button text.
         * @return {@link KeyboardButton} created button.
         */
        private static KeyboardButton getButton(String text) {
            return new KeyboardButton(text);
        }

        /**
         * Creates a {@link KeyboardButton} with given text and web app information.
         *
         * @param text       button text.
         * @param webAppInfo associated {@link WebAppInfo}.
         * @return {@link KeyboardButton} created button.
         */
        private static KeyboardButton getButton(String text, WebAppInfo webAppInfo) {
            KeyboardButton tempButton = new KeyboardButton(text);
            tempButton.setWebApp(webAppInfo);
            return tempButton;
        }

        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        @Getter
        @AllArgsConstructor
        public static class Button {
            String text;
            WebAppInfo webAppInfo;

            public Button(String text) {
                this(text, null);
            }
        }
    }

    /**
     * Class for generating inline keyboards.
     * These are the keyboards that appear in messages with buttons that trigger bot callbacks.
     */
    public static class Inline {

        /**
         * Generates a static inline keyboard with predefined buttons.
         *
         * @param buttonsData 2D array of {@link Button} objects.
         * @return {@link InlineKeyboardMarkup} generated inline keyboard.
         */
        public static InlineKeyboardMarkup getStaticKeyboard(Button[][] buttonsData) {
            List<InlineKeyboardRow> rows = Arrays.stream(buttonsData)
                    .map(row -> new InlineKeyboardRow(Arrays.stream(row)
                            .filter(Objects::nonNull)
                            .map(Inline::getButton)
                            .toArray(InlineKeyboardButton[]::new)
                    )).toList();

            return new InlineKeyboardMarkup(rows);
        }

        /**
         * Generates a resizable inline keyboard with customizable row size.
         *
         * @param buttonsData list of {@link Button} objects.
         * @param rowSize     number of buttons per row.
         * @return {@link InlineKeyboardMarkup} resizable inline keyboard.
         */
        public static InlineKeyboardMarkup getResizableKeyboard(List<Button> buttonsData, int rowSize) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            Deque<Button> buttonsQueue = new LinkedList<>(buttonsData);

            while (!buttonsQueue.isEmpty()) {
                InlineKeyboardRow row = new InlineKeyboardRow(
                        IntStream.range(0, Math.min(buttonsQueue.size(), rowSize))
                                .mapToObj(i -> buttonsQueue.poll())
                                .filter(Objects::nonNull)
                                .map(Inline::getButton)
                                .toArray(InlineKeyboardButton[]::new)
                );

                rows.add(row);
            }

            return new InlineKeyboardMarkup(rows);
        }

        /**
         * Creates a single-button inline keyboard.
         *
         * @param button single {@link Button}.
         * @return {@link InlineKeyboardMarkup} keyboard with a single button.
         */
        public static InlineKeyboardMarkup getSingleButtonKeyboard(Button button) {
            return getStaticKeyboard(new Button[][]{{button}});
        }

        /**
         * Creates a resizable inline keyboard with default row size of 1.
         *
         * @param buttonsData list of {@link Button} objects.
         * @return {@link InlineKeyboardMarkup} resizable inline keyboard.
         */
        public static InlineKeyboardMarkup getResizableKeyboard(List<Button> buttonsData) {
            return getResizableKeyboard(buttonsData, 1);
        }

        /**
         * Helper method to create an {@link InlineKeyboardButton} with a callback data string.
         *
         * @param text         button text.
         * @param callbackData callback data.
         * @param id           unique button identifier.
         * @return {@link InlineKeyboardButton} created inline button.
         */
        private static InlineKeyboardButton getButton(String text, String callbackData, long id) {
            InlineKeyboardButton tempInlineButton = new InlineKeyboardButton(text);
            tempInlineButton.setCallbackData(String.join(Delimiter.SERVICE, String.valueOf(id), callbackData));
            return tempInlineButton;
        }

        private static InlineKeyboardButton getButton(String text, String callbackData, String url, long id) {
            InlineKeyboardButton tempInlineButton = new InlineKeyboardButton(text);
            tempInlineButton.setCallbackData(String.join(Delimiter.SERVICE, String.valueOf(id), callbackData));
            tempInlineButton.setUrl(url);
            return tempInlineButton;
        }

        /**
         * Converts a {@link Button} object to {@link InlineKeyboardButton}.
         *
         * @param button custom {@link Button}.
         * @return {@link InlineKeyboardButton} generated button.
         */
        private static InlineKeyboardButton getButton(Button button) {
            return button.getLink() != null && !button.getLink().isBlank() ?
                    getButton(button.getText(), button.getCallback(), button.getLink(), button.getId()) :
                    getButton(button.getText(), String.join(Delimiter.SERVICE, button.getCallback()), button.getId());
        }

        /**
         * Creates an {@link InlineKeyboardButton} with WebApp info.
         *
         * @param text       button text.
         * @param webAppInfo associated {@link WebAppInfo}.
         * @param id         unique button identifier.
         * @return {@link InlineKeyboardButton} created inline button.
         */
        private static InlineKeyboardButton getButton(String text, WebAppInfo webAppInfo, long id) {
            InlineKeyboardButton tempInlineButton = new InlineKeyboardButton(text);
            tempInlineButton.setWebApp(webAppInfo);
            return tempInlineButton;
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        public static class Button {
            private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

            String text;
            String callback;
            String link;
            long id = ID_GENERATOR.incrementAndGet();

            public Button(String text, String callback, String link) {
                this.text = text;
                this.callback = callback;
                this.link = link;
            }
        }
    }
}
