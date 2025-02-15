package com.serezk4.snp.telegram.bot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

/**
 * Represents a Telegram update with additional utility methods for processing.
 *
 * @author serezk4
 * @version 1.0
 * @since 1.0
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class UpdateProxy {
    Update self;
    QueryType queryType;

    /**
     * Constructs a new {@code TUpdate} instance based on the provided {@code Update}.
     *
     * @param self The original Telegram {@code Update}.
     */
    public UpdateProxy(Update self) {
        this.self = self;
        this.queryType = determineQueryType(self);
    }

    /**
     * Enumeration of possible query types in a Telegram update.
     */
    public enum QueryType {
        MESSAGE, INLINE_QUERY, CHOSEN_INLINE_QUERY, CALLBACK_QUERY,
        EDITED_MESSAGE, CHANNEL_POST, EDITED_CHANNEL_POST, SHIPPING_QUERY,
        PRE_CHECKOUT_QUERY, POLL, POLL_ANSWER, CHAT_JOIN_REQUEST,
        CHAT_MEMBER_UPDATED_MY, CHAT_MEMBER_UPDATED, UNKNOWN;
    }

    /**
     * Determines the query type based on the provided {@code Update}.
     *
     * @param update The Telegram update.
     * @return The corresponding {@code QueryType}.
     */
    private static QueryType determineQueryType(Update update) {
        if (update.hasMessage()) return QueryType.MESSAGE;
        if (update.hasCallbackQuery()) return QueryType.CALLBACK_QUERY;
        if (update.hasInlineQuery()) return QueryType.INLINE_QUERY;
        if (update.hasChosenInlineQuery()) return QueryType.CHOSEN_INLINE_QUERY;
        if (update.hasEditedMessage()) return QueryType.EDITED_MESSAGE;
        if (update.hasChannelPost()) return QueryType.CHANNEL_POST;
        if (update.hasEditedChannelPost()) return QueryType.EDITED_CHANNEL_POST;
        if (update.hasShippingQuery()) return QueryType.SHIPPING_QUERY;
        if (update.hasPreCheckoutQuery()) return QueryType.PRE_CHECKOUT_QUERY;
        if (update.hasPoll()) return QueryType.POLL;
        if (update.hasPollAnswer()) return QueryType.POLL_ANSWER;
        if (update.hasChatJoinRequest()) return QueryType.CHAT_JOIN_REQUEST;
        if (update.hasMyChatMember()) return QueryType.CHAT_MEMBER_UPDATED_MY;
        if (update.hasChatMember()) return QueryType.CHAT_MEMBER_UPDATED;

        return QueryType.UNKNOWN;
    }

    /**
     * Retrieves the message ID from the update based on the query type.
     *
     * @return The message ID, or -1 if not applicable.
     */
    public int getMessageId() {
        return switch (queryType) {
            case MESSAGE -> self.getMessage().getMessageId();
            case CALLBACK_QUERY -> self.getCallbackQuery().getMessage().getMessageId();
            case CHOSEN_INLINE_QUERY -> parseInlineMessageId(self.getChosenInlineQuery().getInlineMessageId());
            case EDITED_MESSAGE -> self.getEditedMessage().getMessageId();
            case CHANNEL_POST -> self.getChannelPost().getMessageId();
            case EDITED_CHANNEL_POST -> self.getEditedChannelPost().getMessageId();
            default -> -1;
        };
    }

    /**
     * Parses the inline message ID to an integer.
     *
     * @param inlineMessageId The inline message ID as a string.
     * @return The parsed message ID, or -1 if parsing fails.
     */
    private int parseInlineMessageId(String inlineMessageId) {
        try {
            return Integer.parseInt(inlineMessageId);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Retrieves the chat ID from the update based on the query type.
     *
     * @return The chat ID, or -1 if not applicable.
     */
    public long getChatId() {
        return switch (queryType) {
            case MESSAGE -> self.getMessage().getChatId();
            case CALLBACK_QUERY -> self.getCallbackQuery().getMessage().getChatId();
            case EDITED_MESSAGE -> self.getEditedMessage().getChatId();
            case CHANNEL_POST -> self.getChannelPost().getChatId();
            case EDITED_CHANNEL_POST -> self.getEditedChannelPost().getChatId();
            case CHAT_JOIN_REQUEST -> self.getChatJoinRequest().getUserChatId();
            case CHAT_MEMBER_UPDATED_MY -> self.getMyChatMember().getChat().getId();
            case CHAT_MEMBER_UPDATED -> self.getChatMember().getChat().getId();
            default -> -1;
        };
    }

    /**
     * Determines if the update is a user message.
     *
     * @return {@code true} if it's a user message, {@code false} otherwise.
     */
    public boolean isUserMessage() {
        return switch (queryType) {
            case MESSAGE -> self.getMessage().isUserMessage();
            case INLINE_QUERY -> !self.getInlineQuery().getFrom().getIsBot();
            default -> false;
        };
    }

    /**
     * Retrieves the username associated with the update.
     *
     * @return The username, or {@code null} if not available.
     */
    public String getUsername() {
        return switch (queryType) {
            case MESSAGE -> self.getMessage().getChat().getUserName();
            case CALLBACK_QUERY -> self.getCallbackQuery().getFrom().getUserName();
            case INLINE_QUERY -> self.getInlineQuery().getFrom().getUserName();
            case CHOSEN_INLINE_QUERY -> self.getChosenInlineQuery().getFrom().getUserName();
            case EDITED_MESSAGE -> self.getEditedMessage().getFrom().getUserName();
            case CHANNEL_POST -> self.getChannelPost().getFrom().getUserName();
            case EDITED_CHANNEL_POST -> self.getEditedChannelPost().getFrom().getUserName();
            case SHIPPING_QUERY -> self.getShippingQuery().getFrom().getUserName();
            case PRE_CHECKOUT_QUERY -> self.getPreCheckoutQuery().getFrom().getUserName();
            case POLL_ANSWER -> self.getPollAnswer().getUser().getUserName();
            case CHAT_JOIN_REQUEST -> self.getChatJoinRequest().getUser().getUserName();
            case CHAT_MEMBER_UPDATED_MY -> self.getMyChatMember().getFrom().getUserName();
            case CHAT_MEMBER_UPDATED -> self.getChatMember().getFrom().getUserName();
            default -> null;
        };
    }

    /**
     * Retrieves the text associated with the update.
     *
     * @return The text content, or {@code null} if not available.
     */
    public String getText() {
        if (self.getMessage() != null && self.getMessage().getWebAppData() != null) {
            return self.getMessage().getWebAppData().getData();
        }

        if (self.hasMessage() && self.getMessage().hasDocument()) {
            return Optional.ofNullable(self.getMessage().getCaption()).orElse("");
        }

        return switch (queryType) {
            case MESSAGE -> self.getMessage().hasText() ? self.getMessage().getText() : null;
            case CALLBACK_QUERY -> self.getCallbackQuery().getData();
            case INLINE_QUERY -> self.getInlineQuery().getQuery();
            case CHOSEN_INLINE_QUERY -> self.getChosenInlineQuery().getQuery();
            case EDITED_MESSAGE -> self.getEditedMessage().getText();
            case CHANNEL_POST -> self.getChannelPost().getText();
            case EDITED_CHANNEL_POST -> self.getEditedChannelPost().getText();
            default -> null;
        };
    }
}
