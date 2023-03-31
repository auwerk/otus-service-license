package org.auwerk.otus.arch.licenseservice.service.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.auwerk.otus.arch.licenseservice.dao.LicenseDao;
import org.auwerk.otus.arch.licenseservice.domain.License;
import org.auwerk.otus.arch.licenseservice.exception.LicenseCreatedByDifferentUserException;
import org.auwerk.otus.arch.licenseservice.exception.LicenseNotFoundException;
import org.auwerk.otus.arch.licenseservice.exception.LicenseQueryAlreadyAccepted;
import org.auwerk.otus.arch.licenseservice.service.LicenseService;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class LicenseServiceImpl implements LicenseService {

    private final PgPool pool;
    private final LicenseDao licenseDao;
    private final SecurityIdentity securityIdentity;

    @Override
    public Uni<List<License>> getAllLicenses(int page, int pageSize) {
        return licenseDao.findAllByUserName(pool, getUserName(), page, pageSize);
    }

    @Override
    public Uni<UUID> createLicense(UUID queryId, String productCode) {
        return pool.withTransaction(
                conn -> licenseDao.findByQueryId(pool, queryId)
                        .onItemOrFailure().transformToUni((license, failure) -> {
                            if (license != null) {
                                throw new LicenseQueryAlreadyAccepted();
                            }
                            return licenseDao.insert(pool, queryId, getUserName(), productCode);
                        }));
    }

    @Override
    public Uni<Void> deleteLicense(UUID licenseId) {
        return licenseDao.findById(pool, licenseId)
                .invoke(license -> {
                    if (!getUserName().equals(license.getUserName())) {
                        throw new LicenseCreatedByDifferentUserException();
                    }
                })
                .flatMap(license -> licenseDao.markDeletedById(pool, license.getId()))
                .onFailure(NoSuchElementException.class)
                .transform(ex -> new LicenseNotFoundException(licenseId));
    }

    private String getUserName() {
        return securityIdentity.getPrincipal().getName();
    }
}
