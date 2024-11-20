package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client")
public class Client extends AbstractPersistable<Long> {

    private Long id;

    @Column(name = "client_id", nullable = false, unique = true, updatable = false)
    private UUID clientId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @OneToMany(mappedBy = "owner")
    private List<Account> accounts;

    @PrePersist
    public void generateClientId() {
        if (clientId == null) {
            clientId = UUID.randomUUID();
        }
    }
}