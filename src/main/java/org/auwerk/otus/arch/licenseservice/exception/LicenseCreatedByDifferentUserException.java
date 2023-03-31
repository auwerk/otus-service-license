package org.auwerk.otus.arch.licenseservice.exception;

public class LicenseCreatedByDifferentUserException extends RuntimeException {

    public LicenseCreatedByDifferentUserException() {
        super("license created by different user");
    }
}
