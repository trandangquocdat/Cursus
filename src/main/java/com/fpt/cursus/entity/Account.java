package com.fpt.cursus.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fpt.cursus.dto.EnrollCourseDto;
import com.fpt.cursus.dto.WishListCourseDto;
import com.fpt.cursus.enums.Gender;
import com.fpt.cursus.enums.status.UserStatus;
import com.fpt.cursus.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String fullName;
    private String avatar;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(unique = true)
    @Email
    private String email;
    private String phone;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.INACTIVE;

    private Date createdDate;
    private Date updatedDate;
    private String updatedBy;

    @Column(columnDefinition = "TEXT")
    private String enrolledCourseJson;
    @Transient
    private List<EnrollCourseDto> enrolledCourse;

    @Column(columnDefinition = "TEXT")
    private String wishListCourseJson;
    @Transient
    private List<WishListCourseDto> wishListCourse;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.role.toString()));
        return authorities;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
