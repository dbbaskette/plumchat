package com.baskettecase.mcpclient.cli;

import com.baskettecase.mcpclient.config.McpConnectionConfigService;
import com.baskettecase.mcpclient.util.ParameterParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CliRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CliRunner.class);


    private final SyncMcpToolCallbackProvider toolCallbackProvider;
    private final ParameterParser parameterParser;
    private final Environment environment;
    private final Scanner scanner = new Scanner(System.in);
    private final McpConnectionConfigService configService;
    private final ConfigurableApplicationContext applicationContext;
    private volatile boolean running = true;

    public CliRunner(SyncMcpToolCallbackProvider toolCallbackProvider,
                     ParameterParser parameterParser, Environment environment, 
                     McpConnectionConfigService configService, ConfigurableApplicationContext applicationContext) {
        this.toolCallbackProvider = toolCallbackProvider;
        this.parameterParser = parameterParser;
        this.environment = environment;
        this.configService = configService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        printWelcome();
        startInteractiveMode();
    }

    private void printWelcome() {
        System.out.println("\n🤖 === Interactive MCP Client === 🤖");
        System.out.println("Directly test your MCP servers without an LLM.");
        System.out.println("Active profile(s): " + String.join(", ", environment.getActiveProfiles()));
        System.out.println("\nAvailable commands:");
        System.out.println("  connect <name> <type> <options>  - Configure a new server (requires rebuild and restart)");
        System.out.println("  list-tools                       - List all available tools from all connected servers");
        System.out.println("  describe-tool <name>             - Show detailed information about a specific tool");
        System.out.println("  tool <name> <json-params>        - Execute a tool with JSON parameters");
        System.out.println("  status                           - Show the status of all connected servers");
        System.out.println("  help                             - Show this help message");
        System.out.println("  exit                             - Exit the client");
        System.out.println();
    }

    private void startInteractiveMode() {
        while (running) {
            System.out.print("mcp-client> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            try {
                processCommand(input);
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                logger.error("Command processing error", e);
            }
        }
    }

    private void processCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "connect" -> handleConnect(args);
            case "list-tools" -> handleListTools();
            case "describe-tool" -> handleDescribeTool(args);
            case "tool" -> handleToolInvocation(args);
            case "status" -> handleStatus();
            case "help" -> printWelcome();
            case "exit", "quit" -> handleExit();
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void handleConnect(String args) {
        if (args.isBlank()) {
            printConnectUsage();
            return;
        }

        String[] parts = args.split("\\s+");
        if (parts.length < 4) {
            printConnectUsage();
            return;
        }

        String name = parts[0];
        String type = parts[1].toLowerCase();

        try {
            switch (type) {
                case "sse":
                    handleSseConnect(name, parts);
                    break;
                case "stdio":
                    handleStdioConnect(name, parts);
                    break;
                default:
                    System.out.println("❌ Unknown transport type: '" + type + "'. Must be 'stdio' or 'sse'.");
                    break;
            }
        } catch (IOException e) {
            System.out.println("❌ Failed to write configuration: " + e.getMessage());
            logger.error("Error writing to properties file", e);
        }
        System.out.println();
    }

    private void printConnectUsage() {
        System.out.println("Usage: connect <name> <type> <options>");
        System.out.println("  <type> can be 'stdio' or 'sse'.");
        System.out.println("\nFor 'stdio':");
        System.out.println("  connect <name> stdio --jar <path/to.jar> [profile]");
        System.out.println("  connect <name> stdio --native <command> [args...]");
        System.out.println("  Example (JAR):   connect myserver stdio --jar /path/to/server.jar dev");
        System.out.println("  Example (native): connect myexec stdio --native /path/to/executable arg1");

        System.out.println("\nFor 'sse':");
        System.out.println("  connect <name> sse --url <http://host:port>");
        System.out.println("  Example: connect myserver sse --url http://localhost:8080");
        System.out.println();
    }

    private void handleSseConnect(String name, String[] parts) throws IOException {
        if (parts.length < 4 || !parts[2].equals("--url")) {
            printConnectUsage();
            return;
        }
        if (configService.sseConnectionExists(name)) {
            System.out.println("⚠️  An SSE connection named '" + name + "' already exists.");
            return;
        }
        String location = parts[3];
        configService.addSseConnection(name, location);
        System.out.println("\n✅ SSE server '" + name + "' configured successfully!");
        System.out.println("   To activate, rebuild and restart the client:");
        System.out.println("   ./mcp-client.sh --rebuild --profile sse");
    }

    private void handleStdioConnect(String name, String[] parts) throws IOException {
        if (parts.length < 4) {
            printConnectUsage();
            return;
        }
        if (configService.stdioConnectionExists(name)) {
            System.out.println("⚠️  An STDIO connection named '" + name + "' already exists.");
            return;
        }

        String subType = parts[2];
        switch (subType) {
            case "--jar":
                if (parts.length < 4) {
                    printConnectUsage();
                    return;
                }
                String jarPath = parts[3];
                String profile = parts.length > 4 ? parts[4] : null;
                configService.addStdioConnection(name, jarPath, profile);
                System.out.println("\n✅ STDIO JAR server '" + name + "' configured successfully!");
                if (profile != null) {
                    System.out.println("   Spring profile: " + profile);
                }
                break;
            case "--native":
                if (parts.length < 4) {
                    printConnectUsage();
                    return;
                }
                String command = parts[3];
                String[] commandArgs = new String[0];
                if (parts.length > 4) {
                    commandArgs = Arrays.copyOfRange(parts, 4, parts.length);
                }
                configService.addStdioNativeConnection(name, command, commandArgs);
                System.out.println("\n✅ STDIO native server '" + name + "' configured successfully!");
                break;
            default:
                System.out.println("❌ Invalid option for stdio. Must be '--jar' or '--native'.");
                printConnectUsage();
                return;
        }

        System.out.println("   To activate, rebuild and restart the client:");
        System.out.println("   ./mcp-client.sh --rebuild --profile stdio");
    }

    private void handleListTools() {
        System.out.println("=== Available Tools ===");
        var toolCallbacks = toolCallbackProvider.getToolCallbacks();
        
        if (toolCallbacks.length == 0) {
            System.out.println("No tools available. Use 'connect' to configure a server and restart.");
            System.out.println();
            return;
        }
        
        System.out.println("Found " + toolCallbacks.length + " tool(s):\n");
        
        for (int i = 0; i < toolCallbacks.length; i++) {
            var callback = toolCallbacks[i];
            var definition = callback.getToolDefinition();
            
            String shortName = extractToolName(definition.name());
            
            System.out.printf("%d. %s\n", i + 1, shortName);
            System.out.printf("   Description: %s\n", definition.description());
            System.out.printf("   Usage: tool %s key=value [key2=value2...] or tool %s '{\"json\":\"object\"}'\n", 
                             shortName, shortName);
            System.out.println();
        }
        
        System.out.println("💡 Tip: Use 'describe-tool <name>' for detailed parameter information");
    }

    private void handleStatus() {
        System.out.println("=== MCP Client Status ===");
        var toolCallbacks = toolCallbackProvider.getToolCallbacks();
        if (toolCallbacks.length == 0) {
            System.out.println("No active server connections. Use 'connect' to configure a server and restart.");
        } else {
            System.out.println("Found " + toolCallbacks.length + " available tool(s) from profile(s): " + String.join(", ", environment.getActiveProfiles()));
            for (var callback : toolCallbacks) {
                System.out.println("✅ Tool: " + callback.getToolDefinition().name() + " - AVAILABLE");
            }
        }
        System.out.println();
    }

    private void handleDescribeTool(String toolName) {
        if (toolName.isBlank()) {
            System.out.println("Usage: describe-tool <tool-name>");
            return;
        }

        org.springframework.ai.tool.ToolCallback targetTool = findToolByName(toolName);

        if (targetTool == null) {
            System.out.println("❌ Tool not found: " + toolName);
            System.out.println("Available tools:");
            var toolCallbacks = toolCallbackProvider.getToolCallbacks();
            for (var callback : toolCallbacks) {
                String shortName = extractToolName(callback.getToolDefinition().name());
                System.out.println("  - " + shortName);
            }
            return;
        }

        var definition = targetTool.getToolDefinition();
        String shortName = extractToolName(definition.name());
        
        System.out.println("\n=== Tool Details ===");
        System.out.println("Name: " + shortName);
        System.out.println("Full name: " + definition.name());
        System.out.println("Description: " + definition.description());
        System.out.println("Usage: tool " + shortName + " <json-params>");
        
        String inputSchema = definition.inputSchema();
        if (inputSchema != null && !inputSchema.trim().isEmpty()) {
            System.out.println("Parameters Schema:");
            System.out.println(formatJsonSchema(inputSchema));
        } else {
            System.out.println("Parameters: No parameters required");
        }
        System.out.println();
    }

    private void handleToolInvocation(String args) {
        if (args.isBlank()) {
            System.out.println("Usage: tool <tool-name> <parameters>");
            System.out.println("Examples:");
            System.out.println("  tool capitalizeText text=\"hello world\"");
            System.out.println("  tool capitalizeText '{\"text\": \"hello world\"}'");
            System.out.println("Tip: Use 'describe-tool <name>' for detailed parameter information");
            return;
        }

        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("❌ Missing parameters. Usage: tool <tool-name> <parameters>");
            return;
        }

        String toolName = parts[0];
        String parameterString = parts[1];

        org.springframework.ai.tool.ToolCallback targetTool = findToolByName(toolName);

        if (targetTool == null) {
            System.out.println("❌ Tool not found: " + toolName);
            System.out.println("Available tools:");
            var toolCallbacks = toolCallbackProvider.getToolCallbacks();
            for (var callback : toolCallbacks) {
                String shortName = extractToolName(callback.getToolDefinition().name());
                System.out.println("  - " + shortName);
            }
            return;
        }

        try {
            String shortName = extractToolName(targetTool.getToolDefinition().name());
            String jsonParameters = convertToJson(parameterString);
            
            System.out.println("🔧 Invoking tool: " + shortName);
            System.out.println("📥 Parameters: " + jsonParameters);
            String result = targetTool.call(jsonParameters);
            System.out.println("✅ Result:");
            System.out.println(result);
            System.out.println();
        } catch (Exception e) {
            System.err.println("❌ Tool invocation failed: " + e.getMessage());
            logger.error("Tool invocation error", e);
        }
    }

    private void handleExit() {
        System.out.println("Shutting down MCP client... Goodbye!");
        // Force exit because applicationContext.close() can hang waiting for child processes.
        System.exit(0);
    }

    private String extractToolName(String fullName) {
        // Extract tool name from patterns like: spring_ai_mcp_client_test_capitalizeText -> capitalizeText
        // Pattern: [prefix]_[connectionName]_[toolName] -> toolName
        if (fullName == null || !fullName.contains("_")) {
            return fullName;
        }
        
        // Split by underscore and take the last part as the tool name
        String[] parts = fullName.split("_");
        return parts[parts.length - 1];
    }

    private String formatJsonSchema(String schema) {
        try {
            // Pretty format the JSON schema for better readability
            var mapper = new ObjectMapper();
            var jsonNode = mapper.readTree(schema);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception e) {
            // If parsing fails, return the original schema
            return schema;
        }
    }

    private String convertToJson(String parameterString) throws Exception {
        // If it's already JSON (starts with { and ends with }), return as-is
        String trimmed = parameterString.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return parameterString;
        }
        
        // Parse key=value pairs and convert to JSON
        // Use the existing ParameterParser to handle key=value parsing
        String[] paramPairs = parseArguments(parameterString);
        var parsedParams = parameterParser.parseParameters(paramPairs);
        
        // Convert to JSON
        var mapper = new ObjectMapper();
        return mapper.writeValueAsString(parsedParams);
    }

    private String[] parseArguments(String input) {
        List<String> args = new ArrayList<>();
        // Regex to match key=value pairs, handling quoted values
        Pattern pattern = Pattern.compile("(\\w+)=(\"[^\"]*\"|'[^']*'|\\S+)");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            args.add(matcher.group(0));
        }
        return args.toArray(new String[0]);
    }

    private org.springframework.ai.tool.ToolCallback findToolByName(String toolName) {
        var toolCallbacks = toolCallbackProvider.getToolCallbacks();
        
        // First try to find by short name
        for (var callback : toolCallbacks) {
            String fullName = callback.getToolDefinition().name();
            String shortName = extractToolName(fullName);
            if (shortName.equals(toolName)) {
                return callback;
            }
        }
        
        // Then try to find by full name
        for (var callback : toolCallbacks) {
            if (callback.getToolDefinition().name().equals(toolName)) {
                return callback;
            }
        }
        
        return null;
    }
}