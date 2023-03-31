package org.auwerk.otus.arch.licenseservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;

import org.auwerk.otus.arch.licenseservice.dao.LicenseDao;
import org.auwerk.otus.arch.licenseservice.domain.License;
import org.auwerk.otus.arch.licenseservice.exception.LicenseCreatedByDifferentUserException;
import org.auwerk.otus.arch.licenseservice.exception.LicenseNotFoundException;
import org.auwerk.otus.arch.licenseservice.exception.LicenseQueryAlreadyAccepted;
import org.auwerk.otus.arch.licenseservice.service.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.SqlConnection;

public class LicenseServiceImplTest {

    private static final String USERNAME = "user";
    private static final String PRODUCT_CODE = "PRODUCT1";

    private final PgPool pool = mock(PgPool.class);
    private final LicenseDao licenseDao = mock(LicenseDao.class);
    private final SecurityIdentity securityIdentity = mock(SecurityIdentity.class);
    private final LicenseService licenseService = new LicenseServiceImpl(pool, licenseDao, securityIdentity);

    @BeforeEach
    void mockTransaction() {
        when(pool.withTransaction(any()))
        .then(inv -> {
            final Function<SqlConnection, Uni<License>> f = inv.getArgument(0);
            return f.apply(null);
        });
    }

    @BeforeEach
    void mockUser() {
        var principal = mock(Principal.class);
        when(principal.getName()).thenReturn(USERNAME);
        when(securityIdentity.getPrincipal()).thenReturn(principal);
    }

    @Test
    void getAllLicenses_success() {
        // given
        final var page = 1;
        final var pageSize = 10;
        final var licenses = List.of(buildLicense(), buildLicense());

        // when
        when(licenseDao.findAllByUserName(pool, USERNAME, page, pageSize))
                .thenReturn(Uni.createFrom().item(licenses));
        final var subscriber = licenseService.getAllLicenses(page, pageSize).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertItem(licenses);
    }

    @Test
    void createLicense_success() {
        // given
        final var queryId = UUID.randomUUID();

        // when
        when(licenseDao.findByQueryId(pool, queryId))
                .thenReturn(Uni.createFrom().failure(new NoSuchElementException()));
        final var subscriber = licenseService.createLicense(queryId, PRODUCT_CODE).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertCompleted();

        verify(licenseDao, times(1))
                .insert(pool, queryId, USERNAME, PRODUCT_CODE);
    }

    @Test
    void createLicense_licenseQueryAlreadyAccepted() {
        // given
        final var queryId = UUID.randomUUID();
        final var license = buildLicense();

        // when
        when(licenseDao.findByQueryId(pool, queryId))
                .thenReturn(Uni.createFrom().item(license));
        final var subscriber = licenseService.createLicense(queryId, PRODUCT_CODE).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertFailedWith(LicenseQueryAlreadyAccepted.class);

        verify(licenseDao, never())
                .insert(pool, queryId, USERNAME, PRODUCT_CODE);
    }

    @Test
    void deleteLicense_success() {
        // given
        final var license = buildLicense();

        // when
        when(licenseDao.findById(pool, license.getId()))
                .thenReturn(Uni.createFrom().item(license));
        final var subscriber = licenseService.deleteLicense(license.getId()).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertCompleted();

        verify(licenseDao, times(1))
                .markDeletedById(pool, license.getId());
    }

    @Test
    void deleteLicense_licenseNotFound() {
        // given
        final var licenseId = UUID.randomUUID();

        // when
        when(licenseDao.findById(pool, licenseId))
                .thenReturn(Uni.createFrom().failure(new NoSuchElementException()));
        final var subscriber = licenseService.deleteLicense(licenseId).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = (LicenseNotFoundException) subscriber
                .assertFailedWith(LicenseNotFoundException.class)
                .getFailure();
        assertEquals(licenseId, failure.getLicenseId());

        verify(licenseDao, never())
                .markDeletedById(pool, licenseId);
    }

    @Test
    void deleteLicense_licenseCreatedByDifferentUser() {
        // given
        final var license = buildLicense();

        // when
        license.setUserName("other-user");
        when(licenseDao.findById(pool, license.getId()))
                .thenReturn(Uni.createFrom().item(license));
        final var subscriber = licenseService.deleteLicense(license.getId()).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertFailedWith(LicenseCreatedByDifferentUserException.class);

        verify(licenseDao, never())
                .markDeletedById(pool, license.getId());
    }

    private static License buildLicense() {
        return License.builder()
                .id(UUID.randomUUID())
                .userName(USERNAME)
                .build();
    }
}
