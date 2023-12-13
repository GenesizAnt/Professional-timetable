package ru.genesizant.Professional.Timetable.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.genesizant.Professional.Timetable.services.PersonDetailsService;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final PersonDetailsService personDetailsService;
    private final JWTFilter jwtFilter;

    public SecurityConfig(PersonDetailsService personDetailsService, JWTFilter jwtFilter) {
        this.personDetailsService = personDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity //.csrf(CsrfConfigurer::disable) //если не отправляется токен с формы
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/super").hasRole("SUPER")
                        .requestMatchers("/admin", "/calendar/*", "/enroll/*", "/specialist/*").hasAnyRole("ADMIN", "SUPER")
                        .requestMatchers("/visitors/*").hasAnyRole("ADMIN", "SUPER", "USER") //ToDo мб поставить просто все залогиныные?
                        .requestMatchers("/auth/login", "/auth/registration", "/error", "/all", "/process_login").permitAll()
                        .anyRequest().hasAnyRole("USER", "ADMIN", "SUPER"))
                .formLogin(login -> login
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/process_login")
                        .defaultSuccessUrl("/auth/check_jwt", true)
                        .failureUrl("/auth/login?error")
                        .permitAll())
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login").permitAll())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));//ToDo разобраться правильно ли так писать и может перестает работать форма регистрации, если стоит STATELESS не сохраняет сессию на сервере

        return httpSecurity.build();
    }

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(personDetailsService)
                .passwordEncoder(getPasswordEncoder());
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //этот код может не работать
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        // настройка провайдера аутентификации
        authenticationProvider.setUserDetailsService(personDetailsService);
        authenticationProvider.setPasswordEncoder(getPasswordEncoder());

        ProviderManager providerManager = new ProviderManager(List.of(authenticationProvider));
        // настройка менеджера провайдеров
        providerManager.setEraseCredentialsAfterAuthentication(true); // очищать учетные данные после аутентификации
        return providerManager;
    }
}
