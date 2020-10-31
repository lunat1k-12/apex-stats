package com.apex.tracker.repository;

import com.apex.tracker.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    @Transactional
    void deleteByTelegramId(Long telegramId);
}
