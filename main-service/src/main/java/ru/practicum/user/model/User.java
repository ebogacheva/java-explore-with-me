package ru.practicum.user.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "users")
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String name;
}

