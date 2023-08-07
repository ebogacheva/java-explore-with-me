package ru.practicum.category.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "categories")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name", unique = true)
    private String name;

}


