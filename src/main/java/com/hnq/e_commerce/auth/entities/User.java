package com.hnq.e_commerce.auth.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hnq.e_commerce.entities.Address;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Table(name = "AUTH_USER_DETAILS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String firstName;

    String lastName;

    String imageUrl;

    @JsonIgnore
    String password;

    Date createdOn;

    Date updatedOn;

    @Column(nullable = false, unique = true)
    String email;

    String phoneNumber;

    String provider;

    String verificationCode;

    boolean enabled = false;

    @ManyToMany
    Set<Role> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    List<Address> addressList;


}
