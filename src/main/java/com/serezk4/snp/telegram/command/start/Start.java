package com.serezk4.snp.telegram.command.start;

import com.serezk4.snp.database.model.User;
import com.serezk4.snp.telegram.bot.Client;
import com.serezk4.snp.telegram.bot.UpdateProxy;
import com.serezk4.snp.telegram.command.SystemCommand;
import com.serezk4.snp.telegram.session.Session;
import com.serezk4.snp.telegram.session.SessionContext;
import com.serezk4.snp.telegram.session.step.Step;
import com.serezk4.snp.telegram.session.step.StepSession;
import com.serezk4.snp.telegram.util.DocumentUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Start extends SystemCommand<StepSession> {
    Client client;

    public Start(final Client client) {
        super(List.of("/start"), "запустить бота");
        this.client = client;
    }

    @Override
    public void execute(
            final UpdateProxy update,
            final SessionContext context,
            final User user
    ) {
        final String fullName = new UpdateProxy(context.getMessages().get(2)).getText();
        final String birthDate = new UpdateProxy(context.getMessages().get(3)).getText();
        final String gender = new UpdateProxy(context.getMessages().get(4)).getText().equals("male")
                ? "Мужской"
                : "Женский";
        final PhotoSize photo = context.getMessages().get(5).getMessage().getPhoto().stream()
                .max(Comparator.comparingInt(PhotoSize::getFileSize))
                .orElseThrow();

        Objects.requireNonNull(photo);
        Objects.requireNonNull(fullName);
        Objects.requireNonNull(birthDate);
        Objects.requireNonNull(gender);

        final String downloadPath = "./downloads/%s.jpg".formatted(photo.getFileId());
        final File localFile = client.downloadFile(photo, downloadPath);
        final String outputPath = "%s.docx".formatted(user.getId().toString().concat("-анкета"));

        DocumentUtil.generateDocument(fullName, birthDate, gender, localFile.getPath(), outputPath);

        client.executeAsync(SendDocument.builder()
                .document(new InputFile(new File(outputPath)))
                .chatId(context.getChatId())
                .caption("Ваша анкета")
                .build());
    }

    @Override
    public Session createSession(Long chatId) {
        return new StepSession(List.of(
                new Step((_, _, _) -> Step.Data.builder()
                        .text("<b>Согласие на обработку данных:</b>\n" +
                                "Нажимая кнопку \"Согласен\" вы соглашаетесь на обработку ваших данных " +
                                "в соответствии с политикой конфиденциальности.")
                        .inlineButtons(List.of(
                                new Step.Button.Inline("Согласен", "agree"),
                                new Step.Button.Inline(
                                        "Подробнее", "",
                                        "https://policies.google.com/privacy?hl=en-US"
                                )
                        ))
                        .inputPattern(".*agree")
                        .errorMessage("Для продолжения вы должны нажать кнопку \"Согласиться\"")
                        .rowSize(2)
                        .build()),
                new Step((_, _, _) -> Step.Data.builder()
                        .text("<b>Введите ФИО:</b>")
                        .inputPattern("[А-Яа-я]+ [А-Яа-я]+( [А-Яа-я]+)?")
                        .errorMessage("Неверный формат ФИО: Фамилия и Имя должны быть заполнены обязательно")
                        .build()),
                new Step((_, _, _) -> Step.Data.builder()
                        .text("<b>Укажите дату рождения в формате dd.MM.yyyy:</b>")
                        .inputPattern("\\d{2}\\.\\d{2}\\.\\d{4}")
                        .errorMessage("Неверный формат даты")
                        .build()),
                new Step((_, _, _) -> Step.Data.builder()
                        .text("<b>Выберите пол:</b>")
                        .inlineButtons(List.of(
                                new Step.Button.Inline("Мужской", "male"),
                                new Step.Button.Inline("Женский", "female")
                        ))
                        .inputPattern(".*male|female")
                        .errorMessage("Выберите один из вариантов")
                        .rowSize(2)
                        .build()),
                new Step((_, _, _) -> Step.Data.builder()
                        .text("<b>Прикрепите фотографию:</b>")
                        .requirePhoto(true)
                        .build()),
                new Step((_, _, context) -> Step.Data.builder()
                        .text("<b>Проверьте введенные данные:</b>%n%s".formatted(
                                context.getMessages().subList(1, context.getMessages().size()).stream()
                                        .map(UpdateProxy::new).map(UpdateProxy::getText)
                                        .filter(text -> text != null && !text.isBlank())
                                        .collect(Collectors.joining("\n"))
                                        .replaceAll(".*\\$agree",
                                                "Согласен обработкой персональных данных"
                                        )
                                        .replaceAll(".*\\$male", "Мужской")
                                        .replaceAll(".*\\$female", "Женский")
                        ))
                        .inlineButtons(List.of(
                                new Step.Button.Inline("Все верно", "all_ok"),
                                new Step.Button.Inline("Отмена", "cancel")
                        ))
                        .rowSize(2)
                        .build())
        ), this, chatId).setSaveUsersMessages(false);
    }
}
