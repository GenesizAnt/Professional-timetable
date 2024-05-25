package ru.genesizant.Professional.Timetable.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.genesizant.Professional.Timetable.model.Person;

import java.util.Collection;
import java.util.Collections;

public record PersonDetails(Person person) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(person.getRole()));
    }

    @Override
    public String getPassword() {
        return this.person.getPassword();
    }

    @Override
    public String getUsername() {
        return this.person.getUsername();
    }

    public String getEmail() {
        return this.person.getEmail();
    }

    public String getRole() {
        return this.person.getRole();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    } //ToDo сделать метод для блокировки аккаунта

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    } //ToDo сделать метод для проверки срока годности пароля

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getJwtToken() {
        return this.person.getJwtToken();
    }

    public Long getId() {
        return this.person.getId();
    }
}

