package com.serezk4.snp.database.dto;

import com.serezk4.snp.database.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.OffsetDateTime;

/**
 * DTO for {@link com.serezk4.snp.database.model.User}
 */
@Value
public class UserDto {
    Long id;

    String username;

    @NotNull(message = "chatId.not_null")
    @Min(message = "chatId.min:0", value = 0)
    Long chatId;

    @NotNull(message = "created.null")
    OffsetDateTime createdAt;

    boolean accountNonLocked;

    @NotNull(message = "role.null")
    User.Role role;

    String utm;
}
