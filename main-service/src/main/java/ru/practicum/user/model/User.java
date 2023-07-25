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
    @Column(name = "id")
    private Long id;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "name")
    private String name;
}

