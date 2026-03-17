package com.malatesh.test.spring_security1;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> {

          auth.requestMatchers("/product/**").permitAll();
          auth.anyRequest().authenticated();

        }).httpBasic(Customizer.withDefaults());

        return http.build();
  }


  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
    UserDetails user = User.withDefaultPasswordEncoder()
        .username("admin1")
        .password("password")
        .roles("admin")
        .build();
    return new InMemoryUserDetailsManager(user);
  }
}
