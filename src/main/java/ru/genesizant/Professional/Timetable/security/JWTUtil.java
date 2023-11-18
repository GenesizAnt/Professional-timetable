package ru.genesizant.Professional.Timetable.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtil {

    @Value("${jwt_secret}")
    private String secret;

    public String generateToken(String username, String email, String phoneNumber) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(1).toInstant());

        return JWT.create()
                .withSubject("User details") //sub (subject) — определяет тему токена.
                .withClaim("username", username) //Payload — это полезные данные, которые хранятся внутри JWT. Эти данные также называют JWT-claims (заявки)
                .withClaim("email", email) //Payload — это полезные данные, которые хранятся внутри JWT. Эти данные также называют JWT-claims (заявки)
                .withClaim("phoneNumber", phoneNumber) //Payload — это полезные данные, которые хранятся внутри JWT. Эти данные также называют JWT-claims (заявки)
                .withIssuedAt(new Date()) // время когда был выдан токен
                .withIssuer("Professional-Timetable") //ToDo поменять если изменится название
                .withExpiresAt(expirationDate)//в какое время закончится срок действия
                .sign(Algorithm.HMAC256(secret));
    }

    public Map<String, String> validateTokenAndRetrieveClaim(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("Professional-Timetable")
                .build();

        DecodedJWT jwt = verifier.verify(token);

        Map<String, String> claims = new HashMap<>();
        claims.put("username", jwt.getClaim("username").asString());
        claims.put("email", jwt.getClaim("email").asString());
        claims.put("phoneNumber", jwt.getClaim("phoneNumber").asString());

        return claims;
//        return jwt.getClaim("username").asString();
    }
}
