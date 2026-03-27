package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

import java.time.LocalDateTime;

/**
 * Base class for all MongoDB documents.
 * Provides common fields and audit information.
 */
@Data
public abstract class BaseDocument {

    @Id
    private String id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    /**
     * MongoDB event listener to set audit fields before saving.
     */
    public static class AuditEventListener extends AbstractMongoEventListener<BaseDocument> {

        @Override
        public void onBeforeConvert(BeforeConvertEvent<BaseDocument> event) {
            BaseDocument document = event.getSource();
            if (document.getCreatedAt() == null) {
                document.setCreatedAt(LocalDateTime.now());
            }
            document.setUpdatedAt(LocalDateTime.now());
        }
    }
}
