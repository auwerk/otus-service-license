package org.auwerk.otus.arch.licenseservice.api;

import static org.mockito.ArgumentMatchers.anyInt;

import java.util.List;
import java.util.UUID;

import org.auwerk.otus.arch.licenseservice.api.dto.CreateLicenseRequestDto;
import org.auwerk.otus.arch.licenseservice.exception.LicenseCreatedByDifferentUserException;
import org.auwerk.otus.arch.licenseservice.exception.LicenseNotFoundException;
import org.auwerk.otus.arch.licenseservice.exception.LicenseQueryAlreadyAcceptedException;
import org.auwerk.otus.arch.licenseservice.service.LicenseService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;

@QuarkusTest
public class LicenseResourceTest extends AbstractAuthenticatedResourceTest {

    private static final String USERNAME = "customer";
    private static final UUID LICENSE_ID = UUID.randomUUID();

    @InjectMock
    LicenseService licenseService;

    @Test
    void getAllLicenses_success() {
        // given
        final var page = 2;
        final var pageSize = 17;

        // when
        Mockito.when(licenseService.getAllLicenses(page, pageSize))
                .thenReturn(Uni.createFrom().item(List.of()));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken(USERNAME))
                .param("page", page)
                .param("pageSize", pageSize)
                .get()
                .then()
                .statusCode(200);
    }

    @Test
    void getAllLicensesDefaultPageParameters_success() {
        // when
        Mockito.when(licenseService.getAllLicenses(anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().item(List.of()));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken(USERNAME))
                .get()
                .then()
                .statusCode(200);

        Mockito.verify(licenseService, Mockito.times(1))
                .getAllLicenses(Integer.valueOf(LicenseResource.DEFAULT_PAGE),
                        Integer.valueOf(LicenseResource.DEFAULT_PAGE_SIZE));
    }

    @Test
    void createLicense_success() {
        // given
        final var request = new CreateLicenseRequestDto(UUID.randomUUID(), "PRODUCT1");

        // when
        Mockito.when(licenseService.createLicense(request.getQueryId(), request.getProductCode()))
                .thenReturn(Uni.createFrom().item(LICENSE_ID));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken(USERNAME))
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .statusCode(200)
                .body("licenseId", Matchers.is(LICENSE_ID.toString()));
    }

    @Test
    void createLicense_licenseQueryAlreadyAccepted() {
        // given
        final var request = new CreateLicenseRequestDto(UUID.randomUUID(), "PRODUCT1");

        // when
        Mockito.when(licenseService.createLicense(request.getQueryId(), request.getProductCode()))
                .thenReturn(Uni.createFrom().failure(new LicenseQueryAlreadyAcceptedException()));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken(USERNAME))
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .statusCode(409)
                .body(Matchers.is("license query already accepted"));
    }

    @Test
    void deleteLicense_success() {
        // when
        Mockito.when(licenseService.deleteLicense(LICENSE_ID))
                .thenReturn(Uni.createFrom().voidItem());

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken(USERNAME))
                .delete("/{licenseId}", LICENSE_ID)
                .then()
                .statusCode(200);
    }

    @Test
    void deleteLicense_licenseNotFound() {
        // when
        Mockito.when(licenseService.deleteLicense(LICENSE_ID))
                .thenReturn(Uni.createFrom().failure(new LicenseNotFoundException(LICENSE_ID)));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken(USERNAME))
                .delete("/{licenseId}", LICENSE_ID)
                .then()
                .statusCode(404)
                .body(Matchers.is("license not found, id=" + LICENSE_ID));
    }

    @Test
    void deleteLicense_licenseCreatedByDifferentUser() {
        // when
        Mockito.when(licenseService.deleteLicense(LICENSE_ID))
                .thenReturn(Uni.createFrom().failure(new LicenseCreatedByDifferentUserException()));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken(USERNAME))
                .delete("/{licenseId}", LICENSE_ID)
                .then()
                .statusCode(403)
                .body(Matchers.is("license created by different user"));
    }
}
