package com.innowise.authservice.mapper;

import com.innowise.authservice.dto.SaveCredentialsRequest;
import com.innowise.authservice.dto.CredentialsResponse;
import com.innowise.authservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(SaveCredentialsRequest request);

    CredentialsResponse toUserResponse(User user);
}
