package me.dblab.twitterclone.config.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static me.dblab.twitterclone.common.Constants.TOKEN_PREFIX;
/**
 * Reference
 * https://github.com/ard333/spring-boot-webflux-jjwt
 * https://www.devglan.com/spring-security/spring-security-webflux-jwt
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final AuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.error(new UnsupportedOperationException("아직 지원하지 않는 기능입니다."));
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        String authHeader = serverHttpRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String authToken = null;

        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            authToken = authHeader.replace(TOKEN_PREFIX, "");
        } else {
            log.warn("couldn't find bearer string, will ingore the header");
        }

        if (authToken != null) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(authToken, authToken);
            return this.authenticationManager.authenticate(authentication)
                    .map(SecurityContextImpl::new);
        }

        return Mono.empty();
    }
}
