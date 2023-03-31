package org.auwerk.otus.arch.licenseservice.api;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.auwerk.otus.arch.licenseservice.api.dto.CreateLicenseRequestDto;
import org.auwerk.otus.arch.licenseservice.api.dto.CreateLicenseResponseDto;
import org.auwerk.otus.arch.licenseservice.exception.LicenseCreatedByDifferentUserException;
import org.auwerk.otus.arch.licenseservice.exception.LicenseNotFoundException;
import org.auwerk.otus.arch.licenseservice.exception.LicenseQueryAlreadyAcceptedException;
import org.auwerk.otus.arch.licenseservice.mapper.LicenseMapper;
import org.auwerk.otus.arch.licenseservice.service.LicenseService;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@Path("/")
@RolesAllowed("${otus.role.customer}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class LicenseResource {

    protected static final String DEFAULT_PAGE = "1";
    protected static final String DEFAULT_PAGE_SIZE = "10";

    private final LicenseService licenseService;
    private final LicenseMapper licenseMapper;

    @GET
    public Uni<Response> getAllLicenses(@QueryParam("page") @DefaultValue(DEFAULT_PAGE) int page,
            @QueryParam("pageSize") @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
        return licenseService.getAllLicenses(page, pageSize)
                .map(licenses -> Response.ok(licenseMapper.toDtos(licenses)).build())
                .onFailure()
                .recoverWithItem(failure -> Response.serverError().entity(failure.getMessage()).build());
    }

    @POST
    public Uni<Response> createLicense(CreateLicenseRequestDto request) {
        return licenseService.createLicense(request.getQueryId(), request.getProductCode())
                .map(licenseId -> Response.ok(new CreateLicenseResponseDto(licenseId)).build())
                .onFailure(LicenseQueryAlreadyAcceptedException.class)
                .recoverWithItem(failure -> Response.status(Status.CONFLICT).entity(failure.getMessage()).build())
                .onFailure()
                .recoverWithItem(failure -> Response.serverError().entity(failure.getMessage()).build());
    }

    @DELETE
    @Path("/{licenseId}")
    public Uni<Response> deleteLicense(@PathParam("licenseId") UUID licenseId) {
        return licenseService.deleteLicense(licenseId).replaceWith(Response.ok().build())
                .onFailure(LicenseNotFoundException.class)
                .recoverWithItem(failure -> Response.status(Status.NOT_FOUND).entity(failure.getMessage()).build())
                .onFailure(LicenseCreatedByDifferentUserException.class)
                .recoverWithItem(failure -> Response.status(Status.FORBIDDEN).entity(failure.getMessage()).build())
                .onFailure()
                .recoverWithItem(failure -> Response.serverError().entity(failure.getMessage()).build());
    }
}
