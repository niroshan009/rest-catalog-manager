package com.kd.iceberg.rest.catalog.manager.repository;

import com.kd.iceberg.rest.catalog.manager.entity.ChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {
}
