package io.opentdf.fory.dto;

import java.util.Objects;

/**
 * Token for authentication/authorization.
 */
public class TokenDto {
    /**
     * Ephemeral id for tracking between request and response.
     */
    private String ephemeralId;

    /**
     * The JWT token.
     */
    private String jwt;

    public TokenDto() {
    }

    public TokenDto(String ephemeralId, String jwt) {
        this.ephemeralId = ephemeralId;
        this.jwt = jwt;
    }

    public String getEphemeralId() {
        return ephemeralId;
    }

    public void setEphemeralId(String ephemeralId) {
        this.ephemeralId = ephemeralId;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenDto tokenDto = (TokenDto) o;
        return Objects.equals(ephemeralId, tokenDto.ephemeralId) && Objects.equals(jwt, tokenDto.jwt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ephemeralId, jwt);
    }

    @Override
    public String toString() {
        return "TokenDto{ephemeralId='" + ephemeralId + "', jwt='[redacted]'}";
    }
}
