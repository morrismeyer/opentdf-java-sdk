package io.opentdf.fory.dto;

import java.util.Objects;

/**
 * Policy binding ensures cryptographic integrity between policy and wrapped key.
 * Prevents policy tampering by binding the policy hash to the encrypted key.
 */
public class PolicyBindingDto {
    /**
     * Cryptographic hashing algorithm used for policy binding.
     * Value: Always "HS256" (HMAC-SHA256).
     */
    private String algorithm;

    /**
     * HMAC-SHA256 hash of the base64-encoded policy using the DEK as the secret key.
     */
    private String hash;

    public PolicyBindingDto() {
    }

    public PolicyBindingDto(String algorithm, String hash) {
        this.algorithm = algorithm;
        this.hash = hash;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyBindingDto that = (PolicyBindingDto) o;
        return Objects.equals(algorithm, that.algorithm) && Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, hash);
    }

    @Override
    public String toString() {
        return "PolicyBindingDto{algorithm='" + algorithm + "', hash='" + hash + "'}";
    }
}
