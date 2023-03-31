package org.auwerk.otus.arch.licenseservice.service;

import java.util.List;
import java.util.UUID;

import org.auwerk.otus.arch.licenseservice.domain.License;

import io.smallrye.mutiny.Uni;

public interface LicenseService {

    /**
     * Получение списка лицензий авторизованного пользователя
     * 
     * @param page     номер страницы
     * @param pageSize количество элементов на странице
     * @return список лицензий
     */
    Uni<List<License>> getAllLicenses(int page, int pageSize);

    /**
     * Создание лицензии на продукт с указанным кодом для авторизованного
     * пользователя
     * 
     * @param queryId     уникальный идентификатор запроса на создание лицензии
     * @param productCode код продукта
     * @return уникальный идентификатор созданной лицензии
     */
    Uni<UUID> createLicense(UUID queryId, String productCode);

    /**
     * Удаление лицензии с указанным уникальным идентификатором у авторизованного
     * пользователя
     * 
     * @param licenseId уникальный идентификатор удаляемой лицензии
     * @return
     */
    Uni<Void> deleteLicense(UUID licenseId);
}
