package de.ingrid.admin.security;


import de.ingrid.admin.Config;
import de.ingrid.admin.JettyInitializer;
import de.ingrid.admin.service.PlugDescriptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${plugdescription.IPLUG_ADMIN_PASSWORD:}")
    String password;

    @Value("${development.mode:false}")
    private boolean developmentMode;


    @Bean
    public ConfigurableServletWebServerFactory servletContainerFactory(Config config) {
        JettyServletWebServerFactory factory = new JettyServletWebServerFactory();
        if (developmentMode) {
            factory.addServerCustomizers(new JettyInitializer());
        }
        factory.setPort(config.webappPort);
        return factory;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, PasswordEncoder passwordEncoder, CustomAuthenticationManager customAuthProvider, InMemoryUserDetailsManager userDetailsManager) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(customAuthProvider);
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsManager);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(Config config, PlugDescriptionService pdService) {
        if (pdService.isIPlugSecured()) {
            UserDetails admin = User.withUsername("admin")
                    .password(config.pdPassword)
                    .roles("admin")
                    .build();
            return new InMemoryUserDetailsManager(admin);
        } else {
            return new InMemoryUserDetailsManager();
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/base/auth/*", "/base/login*", "/css/**", "/images/**", "/js/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/base/auth/login.html")
                .loginProcessingUrl("/base/auth/j_spring_security_check")
                .defaultSuccessUrl("/base/welcome.html", true)
                .failureUrl("/base/auth/loginFailure.html")
                .and()
                .logout()
                .logoutUrl("/perform_logout")
                .deleteCookies("JSESSIONID")
                .and().authenticationManager(authManager);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Component
    public class CustomAuthenticationManager implements AuthenticationProvider {

        private final PlugDescriptionService _plugDescriptionService;

        public CustomAuthenticationManager(PlugDescriptionService _plugDescriptionService) {
            this._plugDescriptionService = _plugDescriptionService;
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            if (_plugDescriptionService.isIPlugSecured()) {
                return null;
            } else {
                return new UsernamePasswordAuthenticationToken(
                        "setup",
                        "initial",
                        List.of(new SimpleGrantedAuthority("ROLE_admin")));
            }
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return true;
        }
    }
}
