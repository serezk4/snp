package com.serezk4.snp.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Entity(name = "users")
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Accessors(chain = true)
public final class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "username")
    String username;

    @Column(name = "chat_id", nullable = false, unique = true)
    @NotNull(message = "chatId.not_null")
    @Min(message = "chatId.min:0", value = 0)
    Long chatId;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    @NotNull(message = "created.null")
    @Temporal(TemporalType.TIMESTAMP)
    OffsetDateTime createdAt = OffsetDateTime.now(ZoneId.systemDefault());

    @Column(name = "account_non_locked", nullable = false)
    @Builder.Default
    @NotNull(message = "account_non_locked.null")
    boolean accountNonLocked = true;

    @Column(name = "role", nullable = false)
    @Builder.Default
    @NotNull(message = "role.null")
    Role role = Role.USER;

    @Column(name = "utm")
    String utm;

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    @Getter
    public enum Role {
        USER(0), ADMIN(Integer.MAX_VALUE);

        int level;

        public static final Role MIN = USER;
        public static final Role MAX = ADMIN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return accountNonLocked == user.accountNonLocked
                && Objects.equals(id, user.id)
                && Objects.equals(username, user.username)
                && Objects.equals(chatId, user.chatId)
                && Objects.equals(createdAt, user.createdAt)
                && role == user.role
                && Objects.equals(utm, user.utm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, chatId, createdAt, accountNonLocked, role, utm);
    }
}
