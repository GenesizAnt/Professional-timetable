package ru.genesizant.Professional.Timetable.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import ru.genesizant.Professional.Timetable.model.Person;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JWTUtil {

    @Value("${jwt_secret}")
    private String secret;

    //Генерация токена с жизненным циклом 60 минут, после клиенту нужно перелогиниться
    public String generateToken(String email) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());
//        Date expirationDate = Date.from(ZonedDateTime.now().plusSeconds(5).toInstant());

        return JWT.create()
                .withSubject("User details") //sub (subject) — определяет тему токена.
//                .withClaim("username", username) //Payload — это полезные данные, которые хранятся внутри JWT. Эти данные также называют JWT-claims (заявки)
                .withClaim("email", email) //Payload — это полезные данные, которые хранятся внутри JWT. Эти данные также называют JWT-claims (заявки)
//                .withClaim("phoneNumber", phoneNumber) //Payload — это полезные данные, которые хранятся внутри JWT. Эти данные также называют JWT-claims (заявки)
                .withIssuedAt(new Date()) // время когда был выдан токен
                .withIssuer("Professional-Timetable") //ToDo поменять если изменится название
                .withExpiresAt(expirationDate)//в какое время закончится срок действия
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndRetrieveClaim(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("Professional-Timetable")
                .build();

        DecodedJWT jwt = verifier.verify(token);

//        Map<String, String> claims = new HashMap<>();
//        claims.put("username", jwt.getClaim("username").asString());
//        claims.put("email", jwt.getClaim("email").asString());
//        claims.put("phoneNumber", jwt.getClaim("phoneNumber").asString());
//
//        return claims;
        return jwt.getClaim("email").asString();
    }

    public boolean isValidJWTAndSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // Получение текущей сессии, если сессия не существует, вернет null

        if (session != null) {
            String jwtToken = (String) session.getAttribute("jwtToken");
            if (jwtToken != null) {
                try {
                    validateTokenAndRetrieveClaim(jwtToken);
                } catch (JWTVerificationException e) {
                    return false;
                    //ToDo какую ошибку тут передать или ничего не надо???
                }
            }
        }
        return true;
    }
}
