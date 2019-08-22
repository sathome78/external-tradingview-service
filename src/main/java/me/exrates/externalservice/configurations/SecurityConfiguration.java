package me.exrates.externalservice.configurations;

import me.exrates.externalservice.properties.SecurityProperties;
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
                                 SecurityProperties securityProperties) {
        this.authenticationProcessFilter = new UserJwtAuthenticationProcessFilter(securityProperties, userService);
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
                .antMatchers("/api/**").hasRole("USER")
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .csrf().disable();
        //@formatter:on
    }
}