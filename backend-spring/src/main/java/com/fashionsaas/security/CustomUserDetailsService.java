package com.fashionsaas.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Servicio que carga los detalles del usuario desde la API de Django.
 * Spring Security lo usa para autenticar al dueño de tienda.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final RestTemplate restTemplate;

    /**
     * Carga el usuario desde la API de Django usando su email.
     * Si no existe, lanza UsernameNotFoundException.
     *
     * @param email correo del usuario a buscar
     * @return UserDetails con la información del usuario
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            String url = "http://localhost:8000/api/auth/me/";
            Map<String, Object> userMap = restTemplate.getForObject(url, Map.class);

            if (userMap == null) {
                throw new UsernameNotFoundException("Usuario no encontrado: " + email);
            }

            String role = (String) userMap.get("role");
            String userEmail = (String) userMap.get("email");

            return new User(
                    userEmail,
                    "",
                    List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            );
        } catch (Exception e) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + email);
        }
    }
}