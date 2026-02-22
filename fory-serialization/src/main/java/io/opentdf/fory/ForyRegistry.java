package io.opentdf.fory;

import io.opentdf.fory.dto.*;
import org.apache.fory.ThreadSafeFory;

/**
 * Registry for all Fory-serializable OpenTDF types.
 * Handles type registration for GraalVM native image compatibility.
 */
public final class ForyRegistry {

    private ForyRegistry() {
        // Utility class
    }

    /**
     * Registers all OpenTDF DTO types with the given Fory instance.
     *
     * @param fory the ThreadSafeFory instance to register types with
     */
    public static void registerTypes(ThreadSafeFory fory) {
        // Core DTOs (register arrays explicitly for better performance)
        fory.register(PolicyBindingDto.class);
        fory.register(KeyAccessDto.class);
        fory.register(EntityDto.class);
        fory.register(EntityDto[].class);  // Explicit array registration for EntityChain
        fory.register(EntityDto.EntityType.class);
        fory.register(EntityDto.Category.class);
        fory.register(EntityChainDto.class);
        fory.register(TokenDto.class);

        // KAS DTOs
        fory.register(RewrapRequestDto.class);
        fory.register(RewrapRequestDto.UnsignedRewrapRequestDto.class);
        fory.register(RewrapRequestDto.WithPolicyDto.class);
        fory.register(RewrapRequestDto.WithKeyAccessObjectDto.class);
        fory.register(RewrapRequestDto.WithPolicyRequestDto.class);
        fory.register(RewrapResponseDto.class);
        fory.register(RewrapResponseDto.PolicyRewrapResultDto.class);
        fory.register(RewrapResponseDto.KeyAccessRewrapResultDto.class);
        fory.register(PublicKeyResponseDto.class);

        // Authorization DTOs
        fory.register(ActionDto.class);
        fory.register(ActionDto.StandardAction.class);
        fory.register(ResourceDto.class);
        fory.register(DecisionRequestDto.class);
        fory.register(DecisionResponseDto.class);
        fory.register(DecisionResponseDto.Decision.class);
        fory.register(EntitlementsResponseDto.class);
        fory.register(EntitlementsResponseDto.EntityEntitlementsDto.class);

        // Policy DTOs
        fory.register(NamespaceDto.class);
        fory.register(AttributeDto.class);
        fory.register(AttributeDto.RuleType.class);
        fory.register(ValueDto.class);
        fory.register(SimpleKasKeyDto.class);
        fory.register(SimpleKasKeyDto.Algorithm.class);
        fory.register(SimpleKasKeyDto.SimpleKasPublicKeyDto.class);
    }

    /**
     * Returns an array of all registered DTO classes.
     * Useful for GraalVM native-image reflection configuration.
     *
     * @return array of all DTO classes
     */
    public static Class<?>[] getAllDtoClasses() {
        return new Class<?>[] {
            // Core DTOs
            PolicyBindingDto.class,
            KeyAccessDto.class,
            EntityDto.class,
            EntityDto[].class,  // Explicit array type for EntityChain
            EntityDto.EntityType.class,
            EntityDto.Category.class,
            EntityChainDto.class,
            TokenDto.class,

            // KAS DTOs
            RewrapRequestDto.class,
            RewrapRequestDto.UnsignedRewrapRequestDto.class,
            RewrapRequestDto.WithPolicyDto.class,
            RewrapRequestDto.WithKeyAccessObjectDto.class,
            RewrapRequestDto.WithPolicyRequestDto.class,
            RewrapResponseDto.class,
            RewrapResponseDto.PolicyRewrapResultDto.class,
            RewrapResponseDto.KeyAccessRewrapResultDto.class,
            PublicKeyResponseDto.class,

            // Authorization DTOs
            ActionDto.class,
            ActionDto.StandardAction.class,
            ResourceDto.class,
            DecisionRequestDto.class,
            DecisionResponseDto.class,
            DecisionResponseDto.Decision.class,
            EntitlementsResponseDto.class,
            EntitlementsResponseDto.EntityEntitlementsDto.class,

            // Policy DTOs
            NamespaceDto.class,
            AttributeDto.class,
            AttributeDto.RuleType.class,
            ValueDto.class,
            SimpleKasKeyDto.class,
            SimpleKasKeyDto.Algorithm.class,
            SimpleKasKeyDto.SimpleKasPublicKeyDto.class
        };
    }
}
