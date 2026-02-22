package io.opentdf.fory.dto;

import java.util.Objects;

/**
 * Simple KAS key containing public key information.
 */
public class SimpleKasKeyDto {
    /**
     * Supported key algorithms.
     */
    public enum Algorithm {
        UNSPECIFIED,
        RSA_2048,
        RSA_4096,
        EC_P256,
        EC_P384,
        EC_P521
    }

    /**
     * The URL of the Key Access Server.
     */
    private String kasUri;

    /**
     * The public key that belongs to the KAS.
     */
    private SimpleKasPublicKeyDto publicKey;

    /**
     * The ID of the Key Access Server.
     */
    private String kasId;

    public SimpleKasKeyDto() {
    }

    public SimpleKasKeyDto(String kasUri, SimpleKasPublicKeyDto publicKey, String kasId) {
        this.kasUri = kasUri;
        this.publicKey = publicKey;
        this.kasId = kasId;
    }

    public String getKasUri() {
        return kasUri;
    }

    public void setKasUri(String kasUri) {
        this.kasUri = kasUri;
    }

    public SimpleKasPublicKeyDto getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(SimpleKasPublicKeyDto publicKey) {
        this.publicKey = publicKey;
    }

    public String getKasId() {
        return kasId;
    }

    public void setKasId(String kasId) {
        this.kasId = kasId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleKasKeyDto that = (SimpleKasKeyDto) o;
        return Objects.equals(kasUri, that.kasUri) &&
               Objects.equals(publicKey, that.publicKey) &&
               Objects.equals(kasId, that.kasId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kasUri, publicKey, kasId);
    }

    @Override
    public String toString() {
        return "SimpleKasKeyDto{kasUri='" + kasUri + "', kasId='" + kasId + "'}";
    }

    /**
     * Simple KAS public key.
     */
    public static class SimpleKasPublicKeyDto {
        /**
         * Algorithm of the key.
         */
        private Algorithm algorithm;

        /**
         * Key identifier.
         */
        private String kid;

        /**
         * PEM-encoded public key.
         */
        private String pem;

        public SimpleKasPublicKeyDto() {
        }

        public SimpleKasPublicKeyDto(Algorithm algorithm, String kid, String pem) {
            this.algorithm = algorithm;
            this.kid = kid;
            this.pem = pem;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        public String getKid() {
            return kid;
        }

        public void setKid(String kid) {
            this.kid = kid;
        }

        public String getPem() {
            return pem;
        }

        public void setPem(String pem) {
            this.pem = pem;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleKasPublicKeyDto that = (SimpleKasPublicKeyDto) o;
            return algorithm == that.algorithm &&
                   Objects.equals(kid, that.kid) &&
                   Objects.equals(pem, that.pem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(algorithm, kid, pem);
        }

        @Override
        public String toString() {
            return "SimpleKasPublicKeyDto{algorithm=" + algorithm + ", kid='" + kid + "'}";
        }
    }
}
