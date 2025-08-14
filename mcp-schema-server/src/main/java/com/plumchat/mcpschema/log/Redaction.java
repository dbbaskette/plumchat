package com.plumchat.mcpschema.log;

public final class Redaction {
    private Redaction() {}

    public static String mask(String input) {
        if (input == null) return null;
        return "*****";
    }
}


