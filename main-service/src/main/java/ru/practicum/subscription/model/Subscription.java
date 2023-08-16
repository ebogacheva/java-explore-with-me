package ru.practicum.subscription.model;

import lombok.*;
import ru.practicum.enums.SubscriptionType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity(name = "subscriptions")
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "subscriber")
    private Long subscriber;

    @NotNull
    @Column(name = "owner")
    private Long owner;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SubscriptionType type;
}
