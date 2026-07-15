package com.codewithkahlil.backend.filter;

import com.codewithkahlil.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor  //make a constructor using fianle feild
public class JwtAuthentificationFilter extends OncePerRequestFilter {

    private final JwtService jwtService ;
    private final  UserDetailsService userDetailsService ;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken ;
        final String userEmail ;

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request , response);
            return ;
        }

        jwtToken = authHeader.substring(7) ;
        userEmail = jwtService.extractUsername(jwtToken);//extract from token
        if (userEmail != null  &&
                SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userdetails =  this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwtToken , userdetails)){
                UsernamePasswordAuthenticationToken authToken = new
                        UsernamePasswordAuthenticationToken(
                                userdetails ,
                        null ,
                        userdetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request , response);
    }
}
