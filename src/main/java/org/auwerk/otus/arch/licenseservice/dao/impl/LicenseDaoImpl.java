package org.auwerk.otus.arch.licenseservice.dao.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.auwerk.otus.arch.licenseservice.dao.LicenseDao;
import org.auwerk.otus.arch.licenseservice.domain.License;
import org.auwerk.otus.arch.licenseservice.exception.DaoException;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

@ApplicationScoped
public class LicenseDaoImpl implements LicenseDao {

    @Override
    public Uni<License> findById(PgPool pool, UUID id) {
        return pool.preparedQuery("SELECT * FROM licenses WHERE id=$1 AND NOT deleted")
                .execute(Tuple.of(id))
                .map(rowSet -> {
                    final var rowSetIterator = rowSet.iterator();
                    if (!rowSetIterator.hasNext()) {
                        throw new NoSuchElementException("license not found, id=" + id);
                    }
                    return mapRow(rowSetIterator.next());
                });
    }

    @Override
    public Uni<License> findByQueryId(PgPool pool, UUID queryId) {
        return pool.preparedQuery("SELECT * FROM licenses WHERE query_id=$1")
                .execute(Tuple.of(queryId))
                .map(rowSet -> {
                    final var rowSetIterator = rowSet.iterator();
                    if (!rowSetIterator.hasNext()) {
                        throw new NoSuchElementException("license not found, queryId=" + queryId);
                    }
                    return mapRow(rowSetIterator.next());
                });
    }

    @Override
    public Uni<List<License>> findAllByUserName(PgPool pool, String userName, int page, int pageSize) {
        return pool.preparedQuery("SELECT * FROM licenses WHERE username=$1 AND NOT deleted")
                .execute(Tuple.of(userName))
                .map(rowSet -> {
                    final var result = new ArrayList<License>(rowSet.rowCount());
                    final var rowSetIterator = rowSet.iterator();
                    while (rowSetIterator.hasNext()) {
                        result.add(mapRow(rowSetIterator.next()));
                    }
                    return result;
                });
    }

    @Override
    public Uni<UUID> insert(PgPool pool, UUID queryId, String userName, String productCode) {
        return pool.preparedQuery("INSERT INTO licenses(id, query_id, username, product_code, created_at) "
                + "VALUES($1, $2, $3, $4, $5) RETURNING id")
                .execute(Tuple.of(UUID.randomUUID(), queryId, userName, productCode, LocalDateTime.now()))
                .map(rowSet -> {
                    final var rowSetIterator = rowSet.iterator();
                    if (!rowSetIterator.hasNext()) {
                        throw new DaoException("license insertion failed");
                    }
                    return rowSetIterator.next().getUUID("id");
                });
    }

    @Override
    public Uni<Void> markDeletedById(PgPool pool, UUID id) {
        return pool.preparedQuery("UPDATE licenses SET deleted=TRUE, deleted_at=$1 WHERE id=$2")
                .execute(Tuple.of(LocalDateTime.now(), id))
                .invoke(rowSet -> {
                    if (rowSet.rowCount() != 1) {
                        throw new DaoException("license marking deleted failed, id=" + id);
                    }
                })
                .replaceWithVoid();
    }

    private static License mapRow(Row row) {
        return License.builder()
                .id(row.getUUID("id"))
                .queryId(row.getUUID("query_id"))
                .userName(row.getString("username"))
                .productCode(row.getString("product_code"))
                .deleted(row.getBoolean("deleted"))
                .createdAt(row.getLocalDateTime("created_at"))
                .deletedAt(row.getLocalDateTime("deleted_at"))
                .build();
    }
}
