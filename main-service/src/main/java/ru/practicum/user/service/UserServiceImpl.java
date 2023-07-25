package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.EWMElementNotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

import static ru.practicum.utils.EWMCommonConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;
import static ru.practicum.utils.EWMCommonMethods.pageRequestOf;

@Service
@RequiredArgsConstructor
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
    public UserDto create(NewUserRequest userRequest) {
        User newUser = mapper.newUserRequestToUser(userRequest);
        User saved = repository.save(newUser);
        return mapper.userToUserDto(saved); // TODO: mapper?
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
        return repository.findAllById(ids);
    }

    private List<UserDto> getAllUsersPaged(Integer from, Integer size) {
        Pageable pageable = pageRequestOf(from, size);
        Page<UserDto> users = repository.findUsers(pageable); // TODO: mapper?
        return mapper.pageToList(users);
    }
}
