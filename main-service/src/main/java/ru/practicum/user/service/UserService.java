package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserWithSubDto;

import java.util.List;

public interface UserService {

    List<UserDto> get(List<Long> ids, Integer from, Integer size);

    UserWithSubDto getUsersWithSubscriptions(Long id);

    UserDto create(NewUserRequest request);

    void delete(Long userId);
}
