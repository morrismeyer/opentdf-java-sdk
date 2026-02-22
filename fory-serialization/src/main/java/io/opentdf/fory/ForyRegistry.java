package io.opentdf.fory;

import io.opentdf.fory.dto.*;
import org.apache.fory.Fory;
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
        // Use registerCallback to register types with the underlying Fory instance
        fory.registerCallback(ForyRegistry::registerTypesOnFory);
    }

    /**
     * Registers types on the underlying Fory instance.
     */
    private static void registerTypesOnFory(Fory f) {
        // For cross-language mode, we need to register with explicit type names
        // that match the Go registration
        f.register(EntityDto.EntityType.class, "io.opentdf.fory.dto.EntityDto$EntityType");
        f.register(EntityDto.Category.class, "io.opentdf.fory.dto.EntityDto$Category");
        f.register(ActionDto.StandardAction.class, "io.opentdf.fory.dto.ActionDto$StandardAction");
        f.register(DecisionResponseDto.Decision.class, "io.opentdf.fory.dto.DecisionResponseDto$Decision");
        f.register(AttributeDto.RuleType.class, "io.opentdf.fory.dto.AttributeDto$RuleType");
        f.register(SimpleKasKeyDto.Algorithm.class, "io.opentdf.fory.dto.SimpleKasKeyDto$Algorithm");

        // Core DTOs
        f.register(PolicyBindingDto.class, "io.opentdf.fory.dto.PolicyBindingDto");
        f.register(KeyAccessDto.class, "io.opentdf.fory.dto.KeyAccessDto");
        f.register(EntityDto.class, "io.opentdf.fory.dto.EntityDto");
        f.register(EntityChainDto.class, "io.opentdf.fory.dto.EntityChainDto");
        f.register(TokenDto.class, "io.opentdf.fory.dto.TokenDto");

        // KAS DTOs
        f.register(RewrapRequestDto.class, "io.opentdf.fory.dto.RewrapRequestDto");
        f.register(RewrapRequestDto.UnsignedRewrapRequestDto.class, "io.opentdf.fory.dto.RewrapRequestDto$UnsignedRewrapRequestDto");
        f.register(RewrapRequestDto.WithPolicyDto.class, "io.opentdf.fory.dto.RewrapRequestDto$WithPolicyDto");
        f.register(RewrapRequestDto.WithKeyAccessObjectDto.class, "io.opentdf.fory.dto.RewrapRequestDto$WithKeyAccessObjectDto");
        f.register(RewrapRequestDto.WithPolicyRequestDto.class, "io.opentdf.fory.dto.RewrapRequestDto$WithPolicyRequestDto");
        f.register(RewrapResponseDto.class, "io.opentdf.fory.dto.RewrapResponseDto");
        f.register(RewrapResponseDto.PolicyRewrapResultDto.class, "io.opentdf.fory.dto.RewrapResponseDto$PolicyRewrapResultDto");
        f.register(RewrapResponseDto.KeyAccessRewrapResultDto.class, "io.opentdf.fory.dto.RewrapResponseDto$KeyAccessRewrapResultDto");
        f.register(PublicKeyResponseDto.class, "io.opentdf.fory.dto.PublicKeyResponseDto");

        // Authorization DTOs
        f.register(ActionDto.class, "io.opentdf.fory.dto.ActionDto");
        f.register(ResourceDto.class, "io.opentdf.fory.dto.ResourceDto");
        f.register(DecisionRequestDto.class, "io.opentdf.fory.dto.DecisionRequestDto");
        f.register(DecisionResponseDto.class, "io.opentdf.fory.dto.DecisionResponseDto");
        f.register(EntitlementsResponseDto.class, "io.opentdf.fory.dto.EntitlementsResponseDto");
        f.register(EntitlementsResponseDto.EntityEntitlementsDto.class, "io.opentdf.fory.dto.EntitlementsResponseDto$EntityEntitlementsDto");

        // Policy DTOs
        f.register(NamespaceDto.class, "io.opentdf.fory.dto.NamespaceDto");
        f.register(AttributeDto.class, "io.opentdf.fory.dto.AttributeDto");
        f.register(ValueDto.class, "io.opentdf.fory.dto.ValueDto");
        f.register(SimpleKasKeyDto.class, "io.opentdf.fory.dto.SimpleKasKeyDto");
        f.register(SimpleKasKeyDto.SimpleKasPublicKeyDto.class, "io.opentdf.fory.dto.SimpleKasKeyDto$SimpleKasPublicKeyDto");
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
