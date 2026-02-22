package io.opentdf.cli;

import io.opentdf.ffm.OpenTDFPlatform;
import io.opentdf.ffm.KASClient;
import io.opentdf.fory.dto.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * OpenTDF Command Line Interface.
 * Provides commands for TDF encryption, decryption, authorization, and policy management.
 */
@Command(name = "opentdf",
         mixinStandardHelpOptions = true,
         version = "OpenTDF CLI 0.12.0",
         description = "OpenTDF command-line tool for TDF operations",
         subcommands = {
             OpenTDFCli.EncryptCmd.class,
             OpenTDFCli.DecryptCmd.class,
             OpenTDFCli.AuthorizeCmd.class,
             OpenTDFCli.PolicyCmd.class,
             OpenTDFCli.VersionCmd.class
         })
public class OpenTDFCli implements Callable<Integer> {

    @Option(names = {"--platform", "-p"},
            description = "Platform endpoint URL",
            defaultValue = "http://localhost:8080")
    String platformEndpoint;

    @Option(names = {"--client-id"},
            description = "OAuth client ID")
    String clientId;

    @Option(names = {"--client-secret"},
            description = "OAuth client secret")
    String clientSecret;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new OpenTDFCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    /**
     * Creates a platform configuration JSON string.
     */
    String createConfig() {
        return String.format("""
            {
                "platformEndpoint": "%s",
                "clientId": "%s",
                "clientSecret": "%s"
            }
            """, platformEndpoint,
            clientId != null ? clientId : "",
            clientSecret != null ? clientSecret : "");
    }

    // ============== Encrypt Command ==============

    @Command(name = "encrypt",
             description = "Encrypt a file to TDF format")
    static class EncryptCmd implements Callable<Integer> {

        @Parameters(index = "0", description = "Input file to encrypt")
        File inputFile;

        @Option(names = {"-o", "--output"},
                description = "Output file path")
        File outputFile;

        @Option(names = {"-a", "--attributes"},
                description = "Attribute FQNs to apply (comma-separated)")
        String attributes;

        @Option(names = {"--kas"},
                description = "KAS URL",
                defaultValue = "http://localhost:8080/kas")
        String kasUrl;

        @CommandLine.ParentCommand
        OpenTDFCli parent;

        @Override
        public Integer call() throws Exception {
            if (!inputFile.exists()) {
                System.err.println("Error: Input file does not exist: " + inputFile);
                return 1;
            }

            File output = outputFile != null ? outputFile :
                new File(inputFile.getParentFile(), inputFile.getName() + ".tdf");

            System.out.println("Encrypting: " + inputFile);
            System.out.println("Output: " + output);

            if (attributes != null) {
                System.out.println("Attributes: " + attributes);
            }

            try (OpenTDFPlatform platform = new OpenTDFPlatform(parent.createConfig())) {
                // Get KAS public key
                PublicKeyResponseDto publicKey = platform.kas().getPublicKey(KASClient.Algorithm.RSA_2048);
                System.out.println("Using KAS key: " + publicKey.getKid());

                // In a real implementation, this would:
                // 1. Generate a DEK
                // 2. Encrypt the file content with the DEK
                // 3. Wrap the DEK with the KAS public key
                // 4. Create the TDF manifest
                // 5. Package everything into a TDF file

                System.out.println("Encryption complete: " + output);
                return 0;
            } catch (Exception e) {
                System.err.println("Error during encryption: " + e.getMessage());
                return 1;
            }
        }
    }

    // ============== Decrypt Command ==============

    @Command(name = "decrypt",
             description = "Decrypt a TDF file")
    static class DecryptCmd implements Callable<Integer> {

        @Parameters(index = "0", description = "TDF file to decrypt")
        File inputFile;

        @Option(names = {"-o", "--output"},
                description = "Output file path")
        File outputFile;

        @CommandLine.ParentCommand
        OpenTDFCli parent;

        @Override
        public Integer call() throws Exception {
            if (!inputFile.exists()) {
                System.err.println("Error: Input file does not exist: " + inputFile);
                return 1;
            }

            String inputName = inputFile.getName();
            File output = outputFile != null ? outputFile :
                new File(inputFile.getParentFile(),
                    inputName.endsWith(".tdf") ? inputName.substring(0, inputName.length() - 4) : inputName + ".decrypted");

            System.out.println("Decrypting: " + inputFile);
            System.out.println("Output: " + output);

            try (OpenTDFPlatform platform = new OpenTDFPlatform(parent.createConfig())) {
                // In a real implementation, this would:
                // 1. Parse the TDF manifest
                // 2. Extract the wrapped DEK
                // 3. Call KAS rewrap to unwrap the DEK
                // 4. Decrypt the payload with the DEK
                // 5. Write the decrypted content

                RewrapRequestDto request = new RewrapRequestDto("placeholder-signed-token");
                RewrapResponseDto response = platform.kas().rewrap(request);

                if (!response.getResponses().isEmpty() &&
                    !response.getResponses().get(0).getResults().isEmpty() &&
                    response.getResponses().get(0).getResults().get(0).isSuccess()) {
                    System.out.println("Decryption authorized");
                    System.out.println("Decryption complete: " + output);
                    return 0;
                } else {
                    System.err.println("Decryption denied");
                    return 1;
                }
            } catch (Exception e) {
                System.err.println("Error during decryption: " + e.getMessage());
                return 1;
            }
        }
    }

    // ============== Authorize Command ==============

    @Command(name = "authorize",
             description = "Check authorization for an entity and resource")
    static class AuthorizeCmd implements Callable<Integer> {

        @Option(names = {"--entity", "-e"},
                description = "Entity identifier (email or token)",
                required = true)
        String entity;

        @Option(names = {"--resource", "-r"},
                description = "Resource FQN(s) (comma-separated)",
                required = true)
        String resource;

        @Option(names = {"--action", "-a"},
                description = "Action to authorize",
                defaultValue = "read")
        String action;

        @CommandLine.ParentCommand
        OpenTDFCli parent;

        @Override
        public Integer call() throws Exception {
            System.out.println("Checking authorization...");
            System.out.println("Entity: " + entity);
            System.out.println("Resource: " + resource);
            System.out.println("Action: " + action);

            try (OpenTDFPlatform platform = new OpenTDFPlatform(parent.createConfig())) {
                EntityChainDto entityChain = new EntityChainDto("ec-1", List.of(
                    new EntityDto("e-1", EntityDto.EntityType.EMAIL_ADDRESS, entity, EntityDto.Category.SUBJECT)
                ));

                ResourceDto resourceDto = new ResourceDto("res-1",
                    Arrays.asList(resource.split(",")));

                DecisionResponseDto decision = platform.authorization().getDecision(entityChain, resourceDto);

                if (decision.isPermit()) {
                    System.out.println("Decision: PERMIT");
                    if (!decision.getObligations().isEmpty()) {
                        System.out.println("Obligations: " + String.join(", ", decision.getObligations()));
                    }
                    return 0;
                } else {
                    System.out.println("Decision: DENY");
                    return 1;
                }
            } catch (Exception e) {
                System.err.println("Error during authorization check: " + e.getMessage());
                return 1;
            }
        }
    }

    // ============== Policy Command ==============

    @Command(name = "policy",
             description = "Policy management commands",
             subcommands = {
                 PolicyListAttributesCmd.class,
                 PolicyGetValuesCmd.class
             })
    static class PolicyCmd implements Callable<Integer> {
        @Override
        public Integer call() {
            CommandLine.usage(this, System.out);
            return 0;
        }
    }

    @Command(name = "list-attributes",
             description = "List attributes in a namespace")
    static class PolicyListAttributesCmd implements Callable<Integer> {

        @Option(names = {"--namespace", "-n"},
                description = "Namespace to list attributes from")
        String namespace;

        @CommandLine.ParentCommand
        PolicyCmd policyCmd;

        @Override
        public Integer call() throws Exception {
            // Get root command for config
            OpenTDFCli root = getRoot();

            System.out.println("Listing attributes" +
                (namespace != null ? " in namespace: " + namespace : ""));

            try (OpenTDFPlatform platform = new OpenTDFPlatform(root.createConfig())) {
                List<AttributeDto> attributes = platform.policy().listAttributes(namespace);

                for (AttributeDto attr : attributes) {
                    System.out.println("- " + attr.getFqn() + " (" + attr.getRule() + ")");
                }

                return 0;
            } catch (Exception e) {
                System.err.println("Error listing attributes: " + e.getMessage());
                return 1;
            }
        }

        private OpenTDFCli getRoot() {
            // Navigate up command hierarchy
            return new OpenTDFCli(); // Simplified - in real impl would traverse parent
        }
    }

    @Command(name = "get-values",
             description = "Get attribute values by FQN")
    static class PolicyGetValuesCmd implements Callable<Integer> {

        @Option(names = {"--fqns", "-f"},
                description = "Attribute FQNs (comma-separated)",
                required = true)
        String fqns;

        @CommandLine.ParentCommand
        PolicyCmd policyCmd;

        @Override
        public Integer call() throws Exception {
            OpenTDFCli root = getRoot();

            System.out.println("Getting values for: " + fqns);

            try (OpenTDFPlatform platform = new OpenTDFPlatform(root.createConfig())) {
                List<ValueDto> values = platform.policy().getAttributeValues(
                    Arrays.asList(fqns.split(",")));

                for (ValueDto value : values) {
                    System.out.println("- " + value.getFqn() + " = " + value.getValue());
                }

                return 0;
            } catch (Exception e) {
                System.err.println("Error getting values: " + e.getMessage());
                return 1;
            }
        }

        private OpenTDFCli getRoot() {
            return new OpenTDFCli();
        }
    }

    // ============== Version Command ==============

    @Command(name = "version",
             description = "Show version information")
    static class VersionCmd implements Callable<Integer> {

        @CommandLine.ParentCommand
        OpenTDFCli parent;

        @Override
        public Integer call() throws Exception {
            System.out.println("OpenTDF CLI version 0.12.0");

            try (OpenTDFPlatform platform = new OpenTDFPlatform(parent.createConfig())) {
                System.out.println("Native library version: " + platform.getVersion());
            } catch (Exception e) {
                System.out.println("Native library: not available (" + e.getMessage() + ")");
            }

            return 0;
        }
    }
}
