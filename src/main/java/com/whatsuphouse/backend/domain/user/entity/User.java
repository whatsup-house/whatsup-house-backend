package com.whatsuphouse.backend.domain.user.entity;

import com.whatsuphouse.backend.global.common.BaseEntity;
import com.whatsuphouse.backend.global.common.enums.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(length = 11)
    private String phone;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    @Builder
    public User(String email, String password, String name, Gender gender, Integer age, String nickname, String phone) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.nickname = nickname;
        this.phone = phone;
    }

    public void updateProfile(String nickname, String phone) {
        this.nickname = nickname;
        this.phone = phone;
    }
}
