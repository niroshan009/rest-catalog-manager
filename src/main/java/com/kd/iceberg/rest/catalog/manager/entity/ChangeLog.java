package com.kd.iceberg.rest.catalog.manager.entity;


import com.kd.iceberg.rest.catalog.manager.constants.ChangeType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Data
@Entity
@Builder
public class ChangeLog {

    @Id
    @GeneratedValue
    private Long id;

    private String changeLogName;

    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    private String icebergTable;

    private String author;

    private String icebergNamespace;

    private String changeDescription;

    private String rollbackStruct;

    private String tableStruct;


    @CreationTimestamp(source = SourceType.DB)
    private Instant createdOn;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedOn;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeLog changeLog = (ChangeLog) o;
        return Objects.equals(changeLogName, changeLog.changeLogName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeLogName);
    }
}
