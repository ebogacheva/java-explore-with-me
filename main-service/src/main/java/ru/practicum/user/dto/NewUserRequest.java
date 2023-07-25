package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class NewUserRequest {

    @NotNull
    @Email
    @Length(min = 6, max = 254)
    private String email;
    @NotNull
    @Length(min = 2, max = 250)
    private String name;
}
