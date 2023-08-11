package ru.practicum.event.location;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> getByLatAndLon(Double lat, Double lon);
}
