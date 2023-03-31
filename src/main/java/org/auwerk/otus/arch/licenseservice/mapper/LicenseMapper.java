package org.auwerk.otus.arch.licenseservice.mapper;

import java.util.List;

import org.auwerk.otus.arch.licenseservice.api.dto.LicenseDto;
import org.auwerk.otus.arch.licenseservice.domain.License;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface LicenseMapper {

    LicenseDto toDto(License license);

    List<LicenseDto> toDtos(List<License> licenses);
}
