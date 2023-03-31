package org.auwerk.otus.arch.licenseservice.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class License {
    private UUID id;
    private UUID queryId;
    private String userName;
    private String productCode;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
