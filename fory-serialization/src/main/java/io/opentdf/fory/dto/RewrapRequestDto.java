package io.opentdf.fory.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Request to rewrap (decrypt and re-encrypt) TDF keys for client access.
 */
public class RewrapRequestDto {
    /**
     * A JWT signed by the DPoP (Demonstration of Proof of Possession) private key.
     */
    private String signedRequestToken;

    public RewrapRequestDto() {
    }

    public RewrapRequestDto(String signedRequestToken) {
        this.signedRequestToken = signedRequestToken;
    }

    public String getSignedRequestToken() {
        return signedRequestToken;
    }

    public void setSignedRequestToken(String signedRequestToken) {
        this.signedRequestToken = signedRequestToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewrapRequestDto that = (RewrapRequestDto) o;
        return Objects.equals(signedRequestToken, that.signedRequestToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signedRequestToken);
    }

    @Override
    public String toString() {
        return "RewrapRequestDto{signedRequestToken='[redacted]'}";
    }

    /**
     * Unsigned rewrap request payload that gets embedded in a JWT.
     */
    public static class UnsignedRewrapRequestDto {
        /**
         * Client's public key in PEM format for establishing a session key.
         */
        private String clientPublicKey;

        /**
         * List of policy requests to be processed.
         */
        private List<WithPolicyRequestDto> requests;

        public UnsignedRewrapRequestDto() {
            this.requests = new ArrayList<>();
        }

        public String getClientPublicKey() {
            return clientPublicKey;
        }

        public void setClientPublicKey(String clientPublicKey) {
            this.clientPublicKey = clientPublicKey;
        }

        public List<WithPolicyRequestDto> getRequests() {
            return requests;
        }

        public void setRequests(List<WithPolicyRequestDto> requests) {
            this.requests = requests;
        }

        public void addRequest(WithPolicyRequestDto request) {
            if (this.requests == null) {
                this.requests = new ArrayList<>();
            }
            this.requests.add(request);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnsignedRewrapRequestDto that = (UnsignedRewrapRequestDto) o;
            return Objects.equals(clientPublicKey, that.clientPublicKey) &&
                   Objects.equals(requests, that.requests);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientPublicKey, requests);
        }
    }

    /**
     * Policy metadata and content for a group of KeyAccessObjects.
     */
    public static class WithPolicyDto {
        /**
         * An identifier unique within the scope of the rewrap request.
         */
        private String id;

        /**
         * Policy content - Base64-encoded JSON policy object.
         */
        private String body;

        public WithPolicyDto() {
        }

        public WithPolicyDto(String id, String body) {
            this.id = id;
            this.body = body;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WithPolicyDto that = (WithPolicyDto) o;
            return Objects.equals(id, that.id) && Objects.equals(body, that.body);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, body);
        }
    }

    /**
     * Key Access Object wrapper with identifier.
     */
    public static class WithKeyAccessObjectDto {
        /**
         * Ephemeral, unique identifier for this KAO within the request.
         */
        private String keyAccessObjectId;

        /**
         * The actual Key Access Object containing cryptographic material and metadata.
         */
        private KeyAccessDto keyAccessObject;

        public WithKeyAccessObjectDto() {
        }

        public WithKeyAccessObjectDto(String keyAccessObjectId, KeyAccessDto keyAccessObject) {
            this.keyAccessObjectId = keyAccessObjectId;
            this.keyAccessObject = keyAccessObject;
        }

        public String getKeyAccessObjectId() {
            return keyAccessObjectId;
        }

        public void setKeyAccessObjectId(String keyAccessObjectId) {
            this.keyAccessObjectId = keyAccessObjectId;
        }

        public KeyAccessDto getKeyAccessObject() {
            return keyAccessObject;
        }

        public void setKeyAccessObject(KeyAccessDto keyAccessObject) {
            this.keyAccessObject = keyAccessObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WithKeyAccessObjectDto that = (WithKeyAccessObjectDto) o;
            return Objects.equals(keyAccessObjectId, that.keyAccessObjectId) &&
                   Objects.equals(keyAccessObject, that.keyAccessObject);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyAccessObjectId, keyAccessObject);
        }
    }

    /**
     * Request grouping policy with associated key access objects.
     */
    public static class WithPolicyRequestDto {
        /**
         * List of Key Access Objects associated with this policy.
         */
        private List<WithKeyAccessObjectDto> keyAccessObjects;

        /**
         * Policy information for this group of KAOs.
         */
        private WithPolicyDto policy;

        /**
         * Cryptographic algorithm identifier for the TDF type.
         */
        private String algorithm;

        public WithPolicyRequestDto() {
            this.keyAccessObjects = new ArrayList<>();
        }

        public List<WithKeyAccessObjectDto> getKeyAccessObjects() {
            return keyAccessObjects;
        }

        public void setKeyAccessObjects(List<WithKeyAccessObjectDto> keyAccessObjects) {
            this.keyAccessObjects = keyAccessObjects;
        }

        public WithPolicyDto getPolicy() {
            return policy;
        }

        public void setPolicy(WithPolicyDto policy) {
            this.policy = policy;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WithPolicyRequestDto that = (WithPolicyRequestDto) o;
            return Objects.equals(keyAccessObjects, that.keyAccessObjects) &&
                   Objects.equals(policy, that.policy) &&
                   Objects.equals(algorithm, that.algorithm);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyAccessObjects, policy, algorithm);
        }
    }
}
