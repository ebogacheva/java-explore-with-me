package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.dto.UserWithSubDto;
import ru.practicum.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toUser(NewUserRequest request);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);

    List<UserShortDto> toShortDtoList(List<User> users);

    @Mapping(target = "subForEvents", ignore = true)
    @Mapping(target = "subForParticipation", ignore = true)
    @Mapping(target = "mySubForEvents", ignore = true)
    @Mapping(target = "mySubForParticipation", ignore = true)
    UserWithSubDto toUserWithSubDto(User user);
}