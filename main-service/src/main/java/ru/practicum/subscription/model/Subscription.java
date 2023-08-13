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
    @Column(name = "user1_id")
    private Long user1Id;

    @NotNull
    @Column(name = "user2_id")
    private Long user2Id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SubscriptionType type;
}
