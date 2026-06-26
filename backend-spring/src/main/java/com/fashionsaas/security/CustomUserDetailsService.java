package com.fashionsaas.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que carga los detalles del usuario desde el email extraído del JWT.
 * Spring Security lo usa para autenticar al dueño de tienda en cada petición.
 * No consulta Django en cada request — el JWT ya fue validado previamente.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Carga el usuario usando el email extraído del JWT.
     * Asigna el rol STORE_OWNER por defecto ya que solo
     * store_owners y admins pueden obtener un JWT de este panel.
     *
     * @param email correo del usuario extraído del JWT
     * @return UserDetails con el email y rol del usuario
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isEmpty()) {
            throw new UsernameNotFoundException("Email no proporcionado.");
        }

        return new User(
                email,
                "",
                List.of(new SimpleGrantedAuthority("ROLE_STORE_OWNER"))
        );
    }
}