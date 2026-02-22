package io.opentdf.fory.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Response containing rewrapped keys and session information.
 */
public class RewrapResponseDto {
    /**
     * KAS's ephemeral session public key in PEM format.
     */
    private String sessionPublicKey;

    /**
     * Policy-grouped rewrap results for the bulk API.
     */
    private List<PolicyRewrapResultDto> responses;

    public RewrapResponseDto() {
        this.responses = new ArrayList<>();
    }

    public String getSessionPublicKey() {
        return sessionPublicKey;
    }

    public void setSessionPublicKey(String sessionPublicKey) {
        this.sessionPublicKey = sessionPublicKey;
    }

    public List<PolicyRewrapResultDto> getResponses() {
        return responses;
    }

    public void setResponses(List<PolicyRewrapResultDto> responses) {
        this.responses = responses;
    }

    public void addResponse(PolicyRewrapResultDto response) {
        if (this.responses == null) {
            this.responses = new ArrayList<>();
        }
        this.responses.add(response);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewrapResponseDto that = (RewrapResponseDto) o;
        return Objects.equals(sessionPublicKey, that.sessionPublicKey) &&
               Objects.equals(responses, that.responses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionPublicKey, responses);
    }

    @Override
    public String toString() {
        return "RewrapResponseDto{sessionPublicKey='" + sessionPublicKey + "', responses=" + responses + "}";
    }

    /**
     * Result for all KAOs associated with a single policy.
     */
    public static class PolicyRewrapResultDto {
        /**
         * Policy identifier matching the policy.id from the request.
         */
        private String policyId;

        /**
         * Results for each KAO under this policy.
         */
        private List<KeyAccessRewrapResultDto> results;

        public PolicyRewrapResultDto() {
            this.results = new ArrayList<>();
        }

        public String getPolicyId() {
            return policyId;
        }

        public void setPolicyId(String policyId) {
            this.policyId = policyId;
        }

        public List<KeyAccessRewrapResultDto> getResults() {
            return results;
        }

        public void setResults(List<KeyAccessRewrapResultDto> results) {
            this.results = results;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PolicyRewrapResultDto that = (PolicyRewrapResultDto) o;
            return Objects.equals(policyId, that.policyId) && Objects.equals(results, that.results);
        }

        @Override
        public int hashCode() {
            return Objects.hash(policyId, results);
        }
    }

    /**
     * Result of a key access object rewrap operation.
     */
    public static class KeyAccessRewrapResultDto {
        /**
         * Metadata associated with this KAO result (e.g., required obligations).
         */
        private Map<String, Object> metadata;

        /**
         * Identifier matching the key_access_object_id from the request.
         */
        private String keyAccessObjectId;

        /**
         * Status of the rewrap operation for this KAO.
         * Values: "permit" (success), "fail" (failure).
         */
        private String status;

        /**
         * Successfully rewrapped key encrypted with the session key.
         * Present when status="permit".
         */
        private byte[] kasWrappedKey;

        /**
         * Error message when rewrap failed.
         * Present when status="fail".
         */
        private String error;

        public KeyAccessRewrapResultDto() {
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }

        public String getKeyAccessObjectId() {
            return keyAccessObjectId;
        }

        public void setKeyAccessObjectId(String keyAccessObjectId) {
            this.keyAccessObjectId = keyAccessObjectId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public byte[] getKasWrappedKey() {
            return kasWrappedKey;
        }

        public void setKasWrappedKey(byte[] kasWrappedKey) {
            this.kasWrappedKey = kasWrappedKey;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public boolean isSuccess() {
            return "permit".equals(status);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyAccessRewrapResultDto that = (KeyAccessRewrapResultDto) o;
            return Objects.equals(metadata, that.metadata) &&
                   Objects.equals(keyAccessObjectId, that.keyAccessObjectId) &&
                   Objects.equals(status, that.status) &&
                   Arrays.equals(kasWrappedKey, that.kasWrappedKey) &&
                   Objects.equals(error, that.error);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(metadata, keyAccessObjectId, status, error);
            result = 31 * result + Arrays.hashCode(kasWrappedKey);
            return result;
        }
    }
}
