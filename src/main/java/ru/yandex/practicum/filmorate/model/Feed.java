package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class Feed {

    @NotNull
    private int eventId;

    private int userId;

    private int entityId;

    private String eventType;

    private String operation;

    @NotNull
    private long timestamp;
}
