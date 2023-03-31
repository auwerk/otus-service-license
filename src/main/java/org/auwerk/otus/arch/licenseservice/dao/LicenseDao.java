package org.auwerk.otus.arch.licenseservice.dao;

import java.util.List;
import java.util.UUID;

import org.auwerk.otus.arch.licenseservice.domain.License;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

public interface LicenseDao {

    Uni<License> findById(PgPool pool, UUID id);

    Uni<License> findByQueryId(PgPool pool, UUID queryId);

    Uni<List<License>> findAllByUserName(PgPool pool, String userName, int page, int pageSize);

    Uni<UUID> insert(PgPool pool, UUID queryId, String userName, String productCode);

    Uni<Void> markDeletedById(PgPool pool, UUID id);
}
