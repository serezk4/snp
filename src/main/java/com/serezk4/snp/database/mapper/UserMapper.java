package com.serezk4.snp.database.mapper;

import com.serezk4.snp.database.dto.UserDto;
import com.serezk4.snp.database.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface UserMapper {
    User toEntity(UserDto userDto);

    UserDto toUserDto(User user);
}
