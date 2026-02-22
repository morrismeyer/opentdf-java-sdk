package io.opentdf.fory.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Key Access Object containing cryptographic material and metadata for TDF decryption.
 */
public class KeyAccessDto {
    /**
     * Base64-encoded encrypted metadata containing additional key information.
     */
    private String encryptedMetadata;

    /**
     * Policy binding ensuring cryptographic integrity between policy and wrapped key.
     */
    private PolicyBindingDto policyBinding;

    /**
     * Protocol identifier for the key access mechanism. Typically 'kas'.
     */
    private String protocol;

    /**
     * Type of key wrapping used for the data encryption key.
     * Values: 'wrapped' (RSA-wrapped), 'ec-wrapped' (ECDH-wrapped).
     */
    private String keyType;

    /**
     * URL of the Key Access Server that can unwrap this key.
     */
    private String kasUrl;

    /**
     * Key identifier for the KAS public key used for wrapping.
     */
    private String kid;

    /**
     * Split identifier for key splitting scenarios.
     */
    private String splitId;

    /**
     * Client-generated data encryption key wrapped by KAS.
     */
    private byte[] wrappedKey;

    /**
     * Complete header containing all metadata and policy information.
     */
    private byte[] header;

    /**
     * Ephemeral public key for ECDH key derivation (ec-wrapped type only).
     */
    private String ephemeralPublicKey;

    public KeyAccessDto() {
    }

    public String getEncryptedMetadata() {
        return encryptedMetadata;
    }

    public void setEncryptedMetadata(String encryptedMetadata) {
        this.encryptedMetadata = encryptedMetadata;
    }

    public PolicyBindingDto getPolicyBinding() {
        return policyBinding;
    }

    public void setPolicyBinding(PolicyBindingDto policyBinding) {
        this.policyBinding = policyBinding;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getKasUrl() {
        return kasUrl;
    }

    public void setKasUrl(String kasUrl) {
        this.kasUrl = kasUrl;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getSplitId() {
        return splitId;
    }

    public void setSplitId(String splitId) {
        this.splitId = splitId;
    }

    public byte[] getWrappedKey() {
        return wrappedKey;
    }

    public void setWrappedKey(byte[] wrappedKey) {
        this.wrappedKey = wrappedKey;
    }

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public String getEphemeralPublicKey() {
        return ephemeralPublicKey;
    }

    public void setEphemeralPublicKey(String ephemeralPublicKey) {
        this.ephemeralPublicKey = ephemeralPublicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyAccessDto that = (KeyAccessDto) o;
        return Objects.equals(encryptedMetadata, that.encryptedMetadata) &&
               Objects.equals(policyBinding, that.policyBinding) &&
               Objects.equals(protocol, that.protocol) &&
               Objects.equals(keyType, that.keyType) &&
               Objects.equals(kasUrl, that.kasUrl) &&
               Objects.equals(kid, that.kid) &&
               Objects.equals(splitId, that.splitId) &&
               Arrays.equals(wrappedKey, that.wrappedKey) &&
               Arrays.equals(header, that.header) &&
               Objects.equals(ephemeralPublicKey, that.ephemeralPublicKey);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(encryptedMetadata, policyBinding, protocol, keyType, kasUrl, kid, splitId, ephemeralPublicKey);
        result = 31 * result + Arrays.hashCode(wrappedKey);
        result = 31 * result + Arrays.hashCode(header);
        return result;
    }

    @Override
    public String toString() {
        return "KeyAccessDto{keyType='" + keyType + "', kasUrl='" + kasUrl + "', kid='" + kid + "'}";
    }
}
