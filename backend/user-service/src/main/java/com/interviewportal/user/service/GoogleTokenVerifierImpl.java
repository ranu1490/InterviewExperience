package com.interviewportal.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.interviewportal.user.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Verifies Google ID tokens by delegating to Google's public {@code tokeninfo} endpoint.
 *
 * <p>Why the tokeninfo endpoint: it validates the signature and expiry for us, so we avoid pulling
 * in Google's client library and managing JWK caching by hand — appropriate for this project's
 * scale. At very high volume you would switch to local JWK verification to avoid the network hop
 * (documented as the alternative).
 *
 * <p>If {@code security.google.client-id} is configured we additionally assert the token's
 * audience matches, preventing tokens minted for a different app from being accepted.
 */
@Service
public class GoogleTokenVerifierImpl implements GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifierImpl.class);
    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final RestClient restClient;
    private final String expectedClientId;

    public GoogleTokenVerifierImpl(RestClient restClient,
                                   @Value("${security.google.client-id:}") String expectedClientId) {
        this.restClient = restClient;
        this.expectedClientId = expectedClientId;
    }

    @Override
    public GoogleUserInfo verify(String idToken) {
        try {
            JsonNode body = restClient.get()
                    .uri(TOKEN_INFO_URL + idToken)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null || !body.hasNonNull("sub")) {
                throw new UnauthorizedException("Invalid Google token");
            }

            if (expectedClientId != null && !expectedClientId.isBlank()) {
                String audience = body.path("aud").asText();
                if (!expectedClientId.equals(audience)) {
                    throw new UnauthorizedException("Google token audience mismatch");
                }
            }

            return new GoogleUserInfo(
                    body.path("sub").asText(),
                    body.path("email").asText(),
                    body.path("name").asText(),
                    body.path("picture").asText());
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("Google token verification failed", ex);
            throw new UnauthorizedException("Could not verify Google token");
        }
    }
}
