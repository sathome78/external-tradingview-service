package me.exrates.externalservice.configurations;

import me.exrates.externalservice.entities.enums.UserRole;
import me.exrates.externalservice.properties.SecurityProperty;
import me.exrates.externalservice.services.UserService;
import me.exrates.externalservice.web.RestAccessDeniedHandler;
import me.exrates.externalservice.web.RestAuthenticationEntryPoint;
import me.exrates.externalservice.web.RestAuthenticationProvider;
import me.exrates.externalservice.web.security.AbstractJwtAuthenticationProcessFilter;
import me.exrates.externalservice.web.security.UserJwtAuthenticationProcessFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final AbstractJwtAuthenticationProcessFilter authenticationProcessFilter;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAuthenticationProvider authenticationProvider;

    @Autowired
    public SecurityConfiguration(UserService userService,
                                 RestAccessDeniedHandler accessDeniedHandler,
                                 RestAuthenticationEntryPoint authenticationEntryPoint,
                                 RestAuthenticationProvider authenticationProvider,
                                 SecurityProperty securityProperty) {
        this.authenticationProcessFilter = new UserJwtAuthenticationProcessFilter(securityProperty, userService);
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        http
                .antMatcher("/api/**")
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(authenticationProcessFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/api/register/**", "/api/authorize").anonymous()
                .antMatchers("/api/**").hasAnyRole(Stream.of(UserRole.values()).map(UserRole::name).collect(Collectors.joining(",")))
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .csrf().disable();
        //@formatter:on
    }
}