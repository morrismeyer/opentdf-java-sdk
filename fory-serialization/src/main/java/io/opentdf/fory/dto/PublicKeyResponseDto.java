package io.opentdf.fory.dto;

import java.util.Objects;

/**
 * Response containing a KAS public key.
 */
public class PublicKeyResponseDto {
    /**
     * The public key in PEM format.
     */
    private String publicKey;

    /**
     * Key identifier.
     */
    private String kid;

    public PublicKeyResponseDto() {
    }

    public PublicKeyResponseDto(String publicKey, String kid) {
        this.publicKey = publicKey;
        this.kid = kid;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicKeyResponseDto that = (PublicKeyResponseDto) o;
        return Objects.equals(publicKey, that.publicKey) && Objects.equals(kid, that.kid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicKey, kid);
    }

    @Override
    public String toString() {
        return "PublicKeyResponseDto{kid='" + kid + "'}";
    }
}
