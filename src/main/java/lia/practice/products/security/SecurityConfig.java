package lia.practice.products.security;


// Web security config

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity // Webflux Reactive security configs
//@EnableWebSecurity
public class SecurityConfig {


    // Web security configs (for non-reactive api)
//    @Bean
//    public SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
//        http
//                .cors(AbstractHttpConfigurer::disable) // Temporary dev. setting
//                .csrf(AbstractHttpConfigurer::disable) // Temporary dev. setting
//                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .formLogin(AbstractHttpConfigurer::disable) // Disable default/automatic spring sec user credentials
////                .exceptionHandling(h -> h.authenticationEntryPoint(unauthorizedHandler))
//                .securityMatcher("/**") // Temporary dev. setting
//                .authorizeHttpRequests(registry -> registry
//                        .requestMatchers("/").permitAll()
//                        .anyRequest().authenticated()
//                );
//        return http.build();
//    }


    //---- Webflux Reactive security configs ----//
    //---- Webflux Reactive security configs ----//
    //---- Webflux Reactive security configs ----//

//    @Bean // Needed to disable default user credentials in webflux sec
//    public ReactiveAuthenticationManager authenticationManager() {
//        return authentication -> Mono.empty();
//    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
//                .cors().disable()
                .csrf().disable()
                .formLogin().and() // Enable form login/default credentials
//                .formLogin().disable() // Disable form login/default user credentials
//                .httpBasic().and() // Enable basic http sec (default creds etc)
                .httpBasic().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.POST, "/**").hasRole("ADMIN");
//                .pathMatchers(HttpMethod.GET, "/**").hasRole("ADMIN")
//                .pathMatchers("/**").permitAll(); // Don't permit everything!
        return http.build();
    }
}
