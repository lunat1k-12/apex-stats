package com.apex.tracker.repository;

import com.apex.tracker.entity.StatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StatRepository extends JpaRepository<StatEntity, Long> {

    @Query(nativeQuery = true,
            value = "SELECT * FROM STATS WHERE \n" +
                    "PLATFORM_USER_HANDLE=:name AND\n" +
                    "CREATED = (SELECT max(created) FROM STATS WHERE PLATFORM_USER_HANDLE=:name)")
    Optional<StatEntity> findLastByName(@Param("name") String name);
}
