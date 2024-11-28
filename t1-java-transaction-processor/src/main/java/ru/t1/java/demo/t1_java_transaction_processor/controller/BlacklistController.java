package ru.t1.java.demo.t1_java_transaction_processor.controller;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.t1_java_transaction_processor.model.dto.CheckBlacklistResponse;

import java.util.Random;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/blacklist")
public class BlacklistController {

    @Value("${security.jwt-secret}")
    private String jwtSecret;

    @GetMapping("/check")
    public ResponseEntity<CheckBlacklistResponse> checkClient(@RequestParam UUID clientId, @RequestParam UUID accountId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (!validateServiceToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isBlocked = checkIfUserIsBlocked();

        CheckBlacklistResponse checkBlacklistResponse = CheckBlacklistResponse.builder()
                .blocked(isBlocked)
                .build();
        return new ResponseEntity<>(checkBlacklistResponse, HttpStatus.OK);
    }

    private boolean validateServiceToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean checkIfUserIsBlocked() {
        Random random = new Random();
        double probabilityOfTrue = 0.2;
        return random.nextDouble() < probabilityOfTrue;
    }
}
