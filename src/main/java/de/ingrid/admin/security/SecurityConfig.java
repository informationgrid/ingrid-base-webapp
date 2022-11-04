package de.ingrid.admin.security;


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
// @ImportResource({ "classpath:webSecurityConfig.xml" })
@EnableWebSecurity
public class SecurityConfig {
    
    

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("admin")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/anonymous*")
                .anonymous()
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
//                    .failureHandler(authenticationFailureHandler())
                .and()
                .logout()
                .logoutUrl("/perform_logout")
                .deleteCookies("JSESSIONID");
//                    .logoutSuccessHandler(logoutSuccessHandler());
        // .and()
        // .exceptionHandling().accessDeniedPage("/accessDenied");
        // .exceptionHandling().accessDeniedHandler(accessDeniedHandler());
        return http.build();
    }
/*
        @Bean
        public LogoutSuccessHandler logoutSuccessHandler() {
            return new CustomLogoutSuccessHandler();
        }

        @Bean
        public AccessDeniedHandler accessDeniedHandler() {
            return new CustomAccessDeniedHandler();
        }

        @Bean
        public AuthenticationFailureHandler authenticationFailureHandler() {
            return new CustomAuthenticationFailureHandler();
        }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}