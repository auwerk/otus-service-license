package org.auwerk.otus.arch.licenseservice.exception;

import java.util.UUID;

import lombok.Getter;

public class LicenseNotFoundException extends RuntimeException {

    @Getter
    private final UUID licenseId;

    public LicenseNotFoundException(UUID licenseId) {
        super("license does not exist, id=" + licenseId);
        this.licenseId = licenseId;
    }
}
