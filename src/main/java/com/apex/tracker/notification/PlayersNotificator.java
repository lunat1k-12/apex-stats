package com.apex.tracker.notification;

import com.apex.tracker.entity.StatEntity;

import java.io.ByteArrayOutputStream;

public interface PlayersNotificator {

    void levelUpMessage(StatEntity stat);

    void rankUpMessage(StatEntity stat);

    void rankStatImage(ByteArrayOutputStream stream);
}
