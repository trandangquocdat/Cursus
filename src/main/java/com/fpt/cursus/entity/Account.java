package com.fpt.cursus.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fpt.cursus.enums.Gender;
import com.fpt.cursus.enums.InstructorStatus;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

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
    private String cvLink;
    @Enumerated(EnumType.STRING)
    private InstructorStatus instructorStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd' 'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createdDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd' 'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;

    @Column(columnDefinition = "TEXT")
    private String subscribersJson;

    @Column(columnDefinition = "TEXT")
    private String subscribingJson;

    @Column(columnDefinition = "TEXT")
    private String purchasedCourseJson;

    @Column(columnDefinition = "TEXT")
    private String enrolledCourseJson;

    @Column(columnDefinition = "TEXT")
    private String wishListCourseJson;

    @Column(columnDefinition = "TEXT")
    private String studiedCourseJson;

    @JsonProperty("authority")
    public String getAuthority() {
        Collection<? extends GrantedAuthority> authorities = getAuthorities();
        return authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.role.toString()));
        return authorities;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
