package ru.practicum.users.service;

import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> get(List<Long> ids, Integer from, Integer size);
    UserDto create(NewUserRequest user);
    void delete(Long userId);
}
