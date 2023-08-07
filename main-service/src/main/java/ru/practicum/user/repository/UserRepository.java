package ru.practicum.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.model.Category;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.net.CacheRequest;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Page<User> findAll(Pageable pageable);
    List<User> findAllByIdIn(List<Long> ids);
    Optional<User> findFirst1ByName(String name);
}
