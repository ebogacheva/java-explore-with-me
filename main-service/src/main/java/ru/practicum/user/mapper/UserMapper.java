package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User newUserRequestToUser(NewUserRequest userRequest);

    UserDto userToUserDto(User user);

    UserShortDto userToUserShortDto(User user);

    List<UserDto> pageToList(Page<UserDto> page);

}