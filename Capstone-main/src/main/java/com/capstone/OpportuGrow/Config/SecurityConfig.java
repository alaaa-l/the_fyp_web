package com.capstone.OpportuGrow.Config;

import com.capstone.OpportuGrow.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.capstone.OpportuGrow.Security.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final UserService userService;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(UserService userService, JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.userService = userService;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/**"))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/**")
                                                .permitAll()
                                                .requestMatchers("/api/consultants/**")
                                                .authenticated()
                                                .requestMatchers("/api/appointments/**")
                                                .authenticated()
                                                .requestMatchers("/api/projects/**")
                                                .authenticated()
                                                .requestMatchers(
                                                                "/",
                                                                "/index",
                                                                "/login",
                                                                "/register",
                                                                "/projects",
                                                                "/projects/**",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/uploads/**")
                                                .permitAll()
                                                .requestMatchers("/my-projects",
                                                                "/projects/create/**",
                                                                "/projects/edit/**",
                                                                "/projects/delete/**",
                                                                "/profile",
                                                                "/member/articles/**",
                                                                "/member/consultants/**")
                                                .hasRole("MEMBER")
                                                .requestMatchers(
                                                                "/consultants/**",
                                                                "/recommend/**",
                                                                "/consultant/appointments/**")
                                                .hasRole("CONSULTANT")
                                                .requestMatchers("/admin/**",
                                                                "/decision/**",
                                                                "/api/admin/**")
                                                .hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .usernameParameter("email")
                                                .passwordParameter("password")
                                                .successHandler((request, response, authentication) -> {
                                                        // Redirect حسب role
                                                        boolean isAdmin = authentication.getAuthorities().stream()
                                                                        .anyMatch(a -> a.getAuthority()
                                                                                        .equals("ROLE_ADMIN"));
                                                        boolean isConsultant = authentication.getAuthorities().stream()
                                                                        .anyMatch(a -> a.getAuthority()
                                                                                        .equals("ROLE_CONSULTANT"));
                                                        boolean isMember = authentication.getAuthorities().stream()
                                                                        .anyMatch(a -> a.getAuthority()
                                                                                        .equals("ROLE_MEMBER"));

                                                        if (isAdmin) {
                                                                response.sendRedirect("/admin/dashboard");
                                                        } else if (isConsultant) {
                                                                response.sendRedirect("/consultants/dashboard");
                                                        } else {
                                                                response.sendRedirect("/");
                                                        }
                                                })
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.authenticationProvider(authenticationProvider()).build();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
