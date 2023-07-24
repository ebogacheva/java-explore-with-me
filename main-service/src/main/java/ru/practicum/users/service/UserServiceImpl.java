package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.EWMElementNotFoundException;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.mapper.UserMapper;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "Пользователь не найден или недоступен.";

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public List<UserDto> get(@Nullable List<Long> ids, Integer from, Integer size) {
        if (Objects.nonNull(ids)) {
            return getUsersByIds(ids);
        } else {
            return getAllUsersPaged(from, size);
        }
    }

    @Override
    public UserDto create(NewUserRequest userRequest) {
        User newUser = mapper.newUserRequestToUser(userRequest);
        User saved = repository.save(newUser);
        return mapper.userToUserDto(saved);
    }

    @Override
    public void delete(Long userId) {
        getUserIfExists(userId);
        repository.deleteById(userId);
    }

    private void getUserIfExists(Long userId) {
        repository.findById(userId)
                .orElseThrow(() -> new EWMElementNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private List<UserDto> getUsersByIds(List<Long> ids) {
        List<User> users = repository.findAllById(ids);
        return users.stream()
                .map(mapper::userToUserDto)
                .collect(Collectors.toList());
    }

    private List<UserDto> getAllUsersPaged(Integer from, Integer size) {
        Pageable pageable = pageRequestOf(from, size);
        Page<User> users = repository.findUsers(pageable);
        return mapper.pageToList(users)
                .stream()
                .map(mapper::userToUserDto)
                .collect(Collectors.toList());
    }

    private static Pageable pageRequestOf(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size);
    }
}
