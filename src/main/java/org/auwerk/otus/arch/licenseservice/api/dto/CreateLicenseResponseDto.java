package org.auwerk.otus.arch.licenseservice.api.dto;

import java.util.UUID;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class CreateLicenseResponseDto {
    private UUID licenseId;
}
