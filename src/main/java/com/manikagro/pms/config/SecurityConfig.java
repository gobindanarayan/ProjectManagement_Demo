package com.manikagro.pms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manikagro.pms.domain.Employee;
import com.manikagro.pms.repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    UserDetailsService userDetailsService(EmployeeRepository employees) {
        return username -> {
            Employee employee = employees.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Employee account was not found"));
            return User.withUsername(employee.getUsername()).password(employee.getPasswordHash())
                    .roles(employee.getRole().name()).disabled(!employee.isActive()).build();
        };
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper, AuthenticationManager authenticationManager) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookiePath("/");
        http.authenticationManager(authenticationManager)
                .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository).csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico", "/api/auth/csrf", "/api/auth/login", "/error").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form.loginProcessingUrl("/api/auth/login")
                        .usernameParameter("username").passwordParameter("password")
                        .successHandler((request, response, authentication) -> writeJson(mapper, response, 200, Map.of("message", "Signed in")))
                        .failureHandler((request, response, exception) -> writeJson(mapper, response, 401, Map.of("message", "Username or password is incorrect"))))
                .logout(logout -> logout.logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> writeJson(mapper, response, 200, Map.of("message", "Signed out"))))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) -> writeJson(mapper, response, 401, Map.of("message", "Please sign in to continue")))
                        .accessDeniedHandler((request, response, exception) -> writeJson(mapper, response, 403, Map.of("message", "You do not have permission to perform this action"))))
                .sessionManagement(session -> session.sessionFixation(fixation -> fixation.migrateSession()));
        return http.build();
    }

    private static void writeJson(ObjectMapper mapper, HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), body);
    }

    private static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
        private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, java.util.function.Supplier<CsrfToken> csrfToken) {
            xor.handle(request, response, csrfToken);
            csrfToken.get();
        }
        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            String header = request.getHeader(csrfToken.getHeaderName());
            return StringUtils.hasText(header) ? plain.resolveCsrfTokenValue(request, csrfToken) : xor.resolveCsrfTokenValue(request, csrfToken);
        }
    }
}
