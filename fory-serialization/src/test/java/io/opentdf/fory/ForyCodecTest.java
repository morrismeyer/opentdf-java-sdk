package io.opentdf.fory;

import io.opentdf.fory.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForyCodecTest {

    private ForyCodec codec;

    @BeforeEach
    void setUp() {
        codec = new ForyCodec();
    }

    @Test
    void testPolicyBindingRoundTrip() {
        PolicyBindingDto original = new PolicyBindingDto("HS256", "abc123hash");

        byte[] serialized = codec.serialize(original);
        PolicyBindingDto deserialized = codec.deserialize(serialized, PolicyBindingDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testKeyAccessRoundTrip() {
        KeyAccessDto original = new KeyAccessDto();
        original.setKeyType("wrapped");
        original.setKasUrl("https://kas.example.com");
        original.setKid("key-123");
        original.setProtocol("kas");
        original.setWrappedKey(new byte[]{1, 2, 3, 4, 5});
        original.setPolicyBinding(new PolicyBindingDto("HS256", "hashvalue"));

        byte[] serialized = codec.serialize(original);
        KeyAccessDto deserialized = codec.deserialize(serialized, KeyAccessDto.class);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.getWrappedKey()).isEqualTo(original.getWrappedKey());
    }

    @Test
    void testEntityRoundTrip() {
        EntityDto original = new EntityDto(
            "entity-1",
            EntityDto.EntityType.EMAIL_ADDRESS,
            "user@example.com",
            EntityDto.Category.SUBJECT
        );

        byte[] serialized = codec.serialize(original);
        EntityDto deserialized = codec.deserialize(serialized, EntityDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testEntityChainRoundTrip() {
        EntityDto entity1 = new EntityDto("e1", EntityDto.EntityType.EMAIL_ADDRESS, "bob@example.com", EntityDto.Category.SUBJECT);
        EntityDto entity2 = new EntityDto("e2", EntityDto.EntityType.CLIENT_ID, "client-123", EntityDto.Category.ENVIRONMENT);

        EntityChainDto original = new EntityChainDto("chain-1", Arrays.asList(entity1, entity2));

        byte[] serialized = codec.serialize(original);
        EntityChainDto deserialized = codec.deserialize(serialized, EntityChainDto.class);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.getEntities()).hasSize(2);
    }

    @Test
    void testRewrapRequestRoundTrip() {
        RewrapRequestDto original = new RewrapRequestDto("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...");

        byte[] serialized = codec.serialize(original);
        RewrapRequestDto deserialized = codec.deserialize(serialized, RewrapRequestDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testDecisionResponseRoundTrip() {
        DecisionResponseDto original = new DecisionResponseDto();
        original.setEntityChainId("ec1");
        original.setResourceAttributesId("attr-set-1");
        original.setDecision(DecisionResponseDto.Decision.PERMIT);
        original.setObligations(Arrays.asList("http://example.org/obligation/watermark"));
        original.setAction(ActionDto.standard(ActionDto.StandardAction.TRANSMIT));

        byte[] serialized = codec.serialize(original);
        DecisionResponseDto deserialized = codec.deserialize(serialized, DecisionResponseDto.class);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.isPermit()).isTrue();
        assertThat(deserialized.getObligations()).containsExactly("http://example.org/obligation/watermark");
    }

    @Test
    void testResourceRoundTrip() {
        ResourceDto original = new ResourceDto("resource-1",
            Arrays.asList(
                "https://example.com/attr/classification/value/secret",
                "https://example.com/attr/region/value/us"
            )
        );

        byte[] serialized = codec.serialize(original);
        ResourceDto deserialized = codec.deserialize(serialized, ResourceDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testDecisionRequestRoundTrip() {
        DecisionRequestDto original = new DecisionRequestDto();
        original.addAction(ActionDto.standard(ActionDto.StandardAction.TRANSMIT));

        EntityDto entity = new EntityDto("e1", EntityDto.EntityType.EMAIL_ADDRESS, "user@example.com", EntityDto.Category.SUBJECT);
        original.addEntityChain(new EntityChainDto("ec1", List.of(entity)));

        original.addResourceAttribute(new ResourceDto("res-1", List.of("https://example.com/attr/foo/value/bar")));

        byte[] serialized = codec.serialize(original);
        DecisionRequestDto deserialized = codec.deserialize(serialized, DecisionRequestDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testNamespaceRoundTrip() {
        NamespaceDto original = new NamespaceDto("ns-1", "example.com", "https://example.com");
        original.setActive(true);

        byte[] serialized = codec.serialize(original);
        NamespaceDto deserialized = codec.deserialize(serialized, NamespaceDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testAttributeRoundTrip() {
        AttributeDto original = new AttributeDto();
        original.setId("attr-1");
        original.setName("classification");
        original.setFqn("https://example.com/attr/classification");
        original.setRule(AttributeDto.RuleType.HIERARCHY);
        original.setActive(true);

        byte[] serialized = codec.serialize(original);
        AttributeDto deserialized = codec.deserialize(serialized, AttributeDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testValueRoundTrip() {
        ValueDto original = new ValueDto("val-1", "secret", "https://example.com/attr/classification/value/secret");
        original.setActive(true);

        byte[] serialized = codec.serialize(original);
        ValueDto deserialized = codec.deserialize(serialized, ValueDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testSimpleKasKeyRoundTrip() {
        SimpleKasKeyDto.SimpleKasPublicKeyDto publicKey = new SimpleKasKeyDto.SimpleKasPublicKeyDto(
            SimpleKasKeyDto.Algorithm.RSA_2048,
            "key-1",
            "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgk...\n-----END PUBLIC KEY-----"
        );

        SimpleKasKeyDto original = new SimpleKasKeyDto("https://kas.example.com", publicKey, "kas-1");

        byte[] serialized = codec.serialize(original);
        SimpleKasKeyDto deserialized = codec.deserialize(serialized, SimpleKasKeyDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testPublicKeyResponseRoundTrip() {
        PublicKeyResponseDto original = new PublicKeyResponseDto(
            "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgk...\n-----END PUBLIC KEY-----",
            "key-123"
        );

        byte[] serialized = codec.serialize(original);
        PublicKeyResponseDto deserialized = codec.deserialize(serialized, PublicKeyResponseDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testTokenRoundTrip() {
        TokenDto original = new TokenDto("token-1", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...");

        byte[] serialized = codec.serialize(original);
        TokenDto deserialized = codec.deserialize(serialized, TokenDto.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void testRewrapResponseRoundTrip() {
        RewrapResponseDto original = new RewrapResponseDto();
        original.setSessionPublicKey("-----BEGIN PUBLIC KEY-----\nMIIBIjANBgk...\n-----END PUBLIC KEY-----");

        RewrapResponseDto.PolicyRewrapResultDto policyResult = new RewrapResponseDto.PolicyRewrapResultDto();
        policyResult.setPolicyId("policy-1");

        RewrapResponseDto.KeyAccessRewrapResultDto kaoResult = new RewrapResponseDto.KeyAccessRewrapResultDto();
        kaoResult.setKeyAccessObjectId("kao-1");
        kaoResult.setStatus("permit");
        kaoResult.setKasWrappedKey(new byte[]{10, 20, 30, 40});
        policyResult.setResults(List.of(kaoResult));

        original.addResponse(policyResult);

        byte[] serialized = codec.serialize(original);
        RewrapResponseDto deserialized = codec.deserialize(serialized, RewrapResponseDto.class);

        assertThat(deserialized.getSessionPublicKey()).isEqualTo(original.getSessionPublicKey());
        assertThat(deserialized.getResponses()).hasSize(1);
        assertThat(deserialized.getResponses().get(0).getResults().get(0).isSuccess()).isTrue();
    }

    @Test
    void testEntitlementsResponseRoundTrip() {
        EntitlementsResponseDto original = new EntitlementsResponseDto();
        original.addEntitlement(new EntitlementsResponseDto.EntityEntitlementsDto(
            "entity-1",
            Arrays.asList("https://example.com/attr/foo/value/bar", "https://example.com/attr/color/value/red")
        ));

        byte[] serialized = codec.serialize(original);
        EntitlementsResponseDto deserialized = codec.deserialize(serialized, EntitlementsResponseDto.class);

        assertThat(deserialized).isEqualTo(original);
    }
}
