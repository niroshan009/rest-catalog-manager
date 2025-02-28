package com.kd.iceberg.rest.catalog.manager.repository;

import com.kd.iceberg.rest.catalog.manager.entity.ChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Stack;

@Repository
public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {

    Stack<ChangeLog> findAllByTagOrderByIdAsc(String tag);

}
