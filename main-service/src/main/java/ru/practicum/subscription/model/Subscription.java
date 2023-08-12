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
@Table(name = "subscriptions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user1_id", "user2_id", "type"})
})
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long user1Id;

    @NotNull
    private Long user2Id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SubscriptionType type;
}
