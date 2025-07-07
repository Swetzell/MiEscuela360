package com.miescuela360.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas
                .requestMatchers("/", "/login", "/public/**", "/css/**", "/js/**", "/images/**").permitAll()
                
                // Rutas específicas por rol
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/asistencias/**").hasAnyRole("ADMIN", "MAESTRA")
                .requestMatchers("/alumnos/**").hasAnyRole("ADMIN", "MAESTRA")
                .requestMatchers("/grados/**").hasAnyRole("ADMIN", "MAESTRA")
                .requestMatchers("/secciones/**").hasAnyRole("ADMIN", "MAESTRA")
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/roles/**").hasRole("ADMIN")
                .requestMatchers("/auditoria/**").hasRole("ADMIN")
                .requestMatchers("/pagos/**").hasRole("ADMIN")
                .requestMatchers("/reportes/**").hasRole("ADMIN")
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                // Configura el matcher para permitir GET en /logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login?logout")   
                .invalidateHttpSession(true)         
                .deleteCookies("JSESSIONID")         
                .permitAll()
            );
        
        return http.build();
    }
}