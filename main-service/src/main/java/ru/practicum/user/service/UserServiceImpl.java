package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.EWMConflictException;
import ru.practicum.exception.EWMElementNotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.utils.EWMCommonConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;
import static ru.practicum.utils.EWMCommonMethods.pageRequestOf;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

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
    @Transactional
    public UserDto create(NewUserRequest userRequest) {
        checkUserNameIsUnique(userRequest.getName());
        User newUser = mapper.newUserRequestToUser(userRequest);
        User saved = repository.save(newUser);
        return mapper.userToUserDto(saved);
    }

    private void checkUserNameIsUnique(String name) {
        Optional<User> userWithNameExist = repository.findFirst1ByName(name);
        if (userWithNameExist.isPresent()) {
            throw new EWMConflictException("Пользователь с таким именем уже существует.");
        }
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        getUserIfExists(userId);
        repository.deleteById(userId);
    }

    private void getUserIfExists(Long userId) {
        repository.findById(userId)
                .orElseThrow(() -> new EWMElementNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private List<UserDto> getUsersByIds(List<Long> ids) {
        List<User> users = repository.findAllByIdIn(ids);
        return users.stream().map(mapper::userToUserDto).collect(Collectors.toList());
    }

    private List<UserDto> getAllUsersPaged(Integer from, Integer size) {
        Pageable pageable = pageRequestOf(from, size);
        Page<User> users = repository.findAll(pageable);
        return mapper.pageToList(users)
                .stream()
                .map(mapper::userToUserDto)
                .collect(Collectors.toList());
    }
}
