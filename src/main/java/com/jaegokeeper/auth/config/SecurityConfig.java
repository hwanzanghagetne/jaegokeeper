package com.jaegokeeper.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 세션(JSESSIONID) 기반 인증. 로그인 자격 증명 검증은 기존 LocalService/SocialService가
 * 그대로 담당하고, 이 설정은 "이후 요청에서 로그인 세션이 있는가"만 판단한다.
 * AuthenticationManager/UserDetailsService는 도입하지 않는다.
 *
 * requestMatchers(String...)가 아니라 AntPathRequestMatcher를 명시적으로 쓴다: 문자열 오버로드는
 * MvcRequestMatcher(HandlerMappingIntrospector 필요)로 해석되는데, 이 설정 빈은 root-context.xml의
 * 루트 컨텍스트에 있고 HandlerMappingIntrospector는 DispatcherServlet의 자식 컨텍스트(servlet-context.xml)에만
 * 있어서, 문자열 매처를 쓰면 실제 Tomcat 기동 시 컨텍스트 불일치로 초기화 오류가 날 수 있다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginRequiredEntryPoint loginRequiredEntryPoint;

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository repository) throws Exception {
        http
                .securityContext(context -> context.securityContextRepository(repository))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/stores/**"),
                                AntPathRequestMatcher.antMatcher("/users/**"),
                                AntPathRequestMatcher.antMatcher("/img/**")
                        ).authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(loginRequiredEntryPoint))
                .logout(logout -> logout
                        .logoutUrl("/auth/session/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value()))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                );

        return http.build();
    }
}
