package com.kd.iceberg.rest.catalog.manager.repository;

import com.kd.iceberg.rest.catalog.manager.entity.ChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Stack;

@Repository
public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {

    Stack<ChangeLog> findAllByTagOrderByIdAsc(String tag);


    @Query("SELECT cl FROM ChangeLog cl " +
            "WHERE cl.tag = (SELECT cl2.tag FROM ChangeLog cl2 ORDER BY cl2.id DESC LIMIT 1) " +
            "AND cl.tag = :tag " +
            "ORDER BY cl.id DESC")
    Stack<ChangeLog> findLatestRecordsByTagIdToRollback(String tag);

}
