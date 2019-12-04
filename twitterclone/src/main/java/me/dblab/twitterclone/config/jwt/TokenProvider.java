package me.dblab.twitterclone.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.account.Account;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static me.dblab.twitterclone.common.Constants.ACCESS_TOKEN_VALIDITY_SECONDS;
import static me.dblab.twitterclone.common.Constants.SIGNING_KEY;

@Component
@Slf4j
public class TokenProvider {
    public String getUsernameFromToken(String authToken) throws Exception {
        return getClaimsFromToken(authToken, Claims::getSubject);
    }

    private <T> T getClaimsFromToken(String authToken, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(authToken);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired(String authToken) {
        final Date expiration = getExpirationDateFromToken(authToken);
        return expiration.after(new Date());
    }

    private Date getExpirationDateFromToken(String authToken) {
        return getClaimsFromToken(authToken, Claims::getExpiration);
    }

    public Claims getAllClaimsFromToken(String authToken) {
        return Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(SIGNING_KEY.getBytes()))
                .parseClaimsJws(authToken)
                .getBody();
    }

    public String generateToken(Account account) {
        final Map<String, Object> authorities = new HashMap<>();
        authorities.put("role", account.getRoles());
        return doGenerateToken(authorities, account.getEmail());
    }

    private String doGenerateToken(Map<String, Object> authorities, String username) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + ACCESS_TOKEN_VALIDITY_SECONDS * 1000);

        return Jwts.builder()
                .setClaims(authorities)
                .setSubject(username)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, Base64.getEncoder().encodeToString(SIGNING_KEY.getBytes()))
                .compact();
    }
}
