package com.mballem.curso.security.config;

import com.mballem.curso.security.domain.PerfilTipo;
import com.mballem.curso.security.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String ADMIN = PerfilTipo.ADMIN.getDesc();
    private static final String MEDICOS = PerfilTipo.MEDICO.getDesc();
    private static final String PACIENTE = PerfilTipo.PACIENTE.getDesc();


    @Autowired
    private UsuarioService service;
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/webjars/**", "/css/**", "/image/**", "/js/**").permitAll()
                .antMatchers("/", "/home").permitAll()

                //acesso privado a admin
                .antMatchers("/u/**").hasAuthority("ADMIN")
                //acesso privado a medicos
                .antMatchers("/medicos/**").hasAuthority("MEDICO")
                 .antMatchers("/especialidades/**").hasAuthority("ADMIN")

                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login-error")
                .permitAll()

                .and()
                .logout()
                .logoutSuccessUrl("/")

                .and()
                .exceptionHandling()
                .accessDeniedPage("/acesso-negado")
                ;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(service)
                .passwordEncoder(new BCryptPasswordEncoder());
    }
}
