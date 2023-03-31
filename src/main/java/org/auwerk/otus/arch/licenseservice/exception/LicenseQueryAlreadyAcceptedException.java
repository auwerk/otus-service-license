package org.auwerk.otus.arch.licenseservice.exception;

public class LicenseQueryAlreadyAcceptedException extends RuntimeException {

    public LicenseQueryAlreadyAcceptedException() {
        super("license query already accepted");
    }
}
