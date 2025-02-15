package com.serezk4.snp.telegram.bot;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

@Component
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Client extends OkHttpTelegramClient {
    String botToken;

    public Client(final @Value("${telegram.bot.token}") String botToken) {
        super(botToken);
        this.botToken = botToken;
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> CompletableFuture<T> executeAsync(Method method) {
        try {
            log.info("Executed method (async): {}", method.getClass().getSimpleName());
            return super.executeAsync(method);
        } catch (TelegramApiException e) {
            log.error("Error while executing method: {}. Error is {}", method.getMethod(), e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> T executeWithException(
            final Method method
    ) throws TelegramApiException {
        log.info("Executed method: {}", method.getClass().getSimpleName());
        return super.execute(method);
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) {
        try {
            log.info("Executed method (not async): {}", method.getClass().getSimpleName());
            return super.execute(method);
        } catch (TelegramApiException e) {
            log.error("Error while executing method: {}. Error is {}", method.getMethod(), e.getMessage());
            return null;
        }
    }

    public java.io.File downloadFile(PhotoSize photo, String localFilePath) {
        try {
            if (localFilePath == null || localFilePath.trim().isEmpty()) {
                log.error("Error: localFilePath is null or empty.");
                return null;
            }

            File file = execute(new GetFile(photo.getFileId()));
            if (file == null || file.getFilePath() == null || file.getFilePath().isEmpty()) {
                log.error("Error: Telegram did not return a valid file path for fileId: {}", photo.getFileId());
                return null;
            }

            String fileUrl = "https://api.telegram.org/file/bot" + this.botToken + "/" + file.getFilePath();
            log.info("Attempting to download from: {}", fileUrl);

            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fileUrl))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                log.error("Failed to download file. HTTP response code: {}", response.statusCode());
                return null;
            }

            Path outputPath = Paths.get(localFilePath).toAbsolutePath();

            // Ensure directory exists
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }

            // Ensure file permissions allow writing
            java.io.File localFile = outputPath.toFile();
            if (localFile.exists() && !localFile.canWrite()) {
                log.error("Error: No write permission for file {}", localFile.getAbsolutePath());
                return null;
            }

            // Copy file to disk
            Files.copy(response.body(), outputPath, StandardCopyOption.REPLACE_EXISTING);

            // Verify file downloaded correctly
            if (!localFile.exists() || localFile.length() == 0) {
                log.error("File download failed! File is empty.");
                return null;
            }

            log.info("File downloaded successfully: {}", localFile.getAbsolutePath());
            return localFile;

        } catch (IOException | InterruptedException e) {
            log.error("Error while downloading file: {}", e.getMessage(), e);
            return null;
        }
    }

    public void sendMessage(long chatId, String text) {
        executeAsync(SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build()
        );
    }
}
