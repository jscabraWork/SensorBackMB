package com.arquitectura;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter{

	@Autowired
	private ReactiveAuthenticationManager authenticationManager;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
				.filter(authHeader -> authHeader.startsWith("Bearer "))
				.switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
				.map(token -> token.replace("Bearer ", ""))
				.flatMap(token ->
						authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(null, token))
								.flatMap(authentication -> chain.filter(exchange)
										.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
								)
								.onErrorResume(e -> {
									if (e.getMessage().contains("expired")) {
										// Si el token ha expirado, retorna un 401 Unauthorized
										exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(401));
									} else {
										// Si el token es inválido (por ejemplo, no se pudo autenticar), retorna un 403 Forbidden
										exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(403));
									}
									return exchange.getResponse().setComplete();
								})
				);
	}

}
