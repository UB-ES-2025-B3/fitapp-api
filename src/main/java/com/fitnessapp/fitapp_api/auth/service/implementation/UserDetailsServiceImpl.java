package com.fitnessapp.fitapp_api.auth.service.implementation;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAuthRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Tenemos User sec y necesitamos devolver UserDetails
        // Traemos el usuario de la bd
        UserAuth userAuth = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + "no fue encontrado"));

        // Con GrantedAuthority Spring Security maneja permisos
        Set<GrantedAuthority> authorityList = new LinkedHashSet<>();

        // Si en algún momento usamos roles:
        // Tomamos roles y los convertimos en SimpleGrantedAuthority para poder agregarlos a la authorityList
        //userAuth.getRolesList()
        //        .forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleName()))));

        // Retornamos el usuario en formato Spring Security con los datos de nuestro userSec.
        // User es una implementación de UserDetails
        return new User(userAuth.getEmail(),
                userAuth.getPassword(),
                userAuth.isEnabled(),
                userAuth.isAccountNonExpired(),
                userAuth.isCredentialsNonExpired(),
                userAuth.isAccountNonLocked(),
                authorityList);
    }
}