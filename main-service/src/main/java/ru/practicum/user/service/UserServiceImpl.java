package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ExploreConflictException;
import ru.practicum.exception.ExploreNotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.utils.ExploreConstantsAndStaticMethods.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(NewUserRequest request) {
        checkUserNameIsUnique(request.getName());
        User newUser = userMapper.toUser(request);
        User savedUser = userRepository.save(newUser);
        return userMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> get(@Nullable List<Long> ids, Integer from, Integer size) {
        if (Objects.nonNull(ids)) {
            return getUsersByIds(ids);
        } else {
            return getAllUsersPaged(from, size);
        }
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        User user = getUserIfExists(userId);
        userRepository.delete(user);
    }

    private void checkUserNameIsUnique(String name) {
        userRepository.findFirst1ByName(name).ifPresent((user) -> {
            throw new ExploreConflictException(USER_NAME_ALREADY_EXISTS);
        });
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExploreNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private List<UserDto> getUsersByIds(List<Long> ids) {
        List<User> users = userRepository.findAllByIdIn(ids);
        return users.stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    private List<UserDto> getAllUsersPaged(Integer from, Integer size) {
        Page<User> users = userRepository.findAll(pageRequestOf(from, size));
        return users.map(userMapper::toUserDto).getContent();
    }
}
