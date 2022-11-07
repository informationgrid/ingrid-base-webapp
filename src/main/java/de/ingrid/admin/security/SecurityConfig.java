package de.ingrid.admin.security;


import de.ingrid.admin.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Value("${plugdescription.IPLUG_ADMIN_PASSWORD:}") String password;

    @Bean
    public InMemoryUserDetailsManager userDetailsService(Config config) {
        UserDetails admin = User.withUsername("admin")
                .password(config.pdPassword)
                .roles("admin")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/base/auth/*", "/base/login*", "/css/**", "/images/**", "/js/**")
                    .permitAll()
                .anyRequest()
                    .authenticated()
                .and()
                .formLogin()
                .loginPage("/base/auth/login.html")
                .loginProcessingUrl("/j_spring_security_check")
                .defaultSuccessUrl("/base/welcome.html", true)
                .failureUrl("/base/auth/loginFailure.html")
                .and()
                .logout()
                .logoutUrl("/perform_logout")
                .deleteCookies("JSESSIONID");
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}