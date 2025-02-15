package com.serezk4.snp.telegram.session;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@RequiredArgsConstructor
@Builder
public final class SessionContext {
    List<Update> messages = new LinkedList<>();
    Map<String, Object> data = new HashMap<>();
    Long chatId;
}
