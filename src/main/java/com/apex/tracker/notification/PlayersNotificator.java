package com.apex.tracker.notification;

import com.apex.tracker.entity.StatEntity;

public interface PlayersNotificator {

    void levelUpMessage(StatEntity stat);

    void rankUpMessage(StatEntity stat);
}
