package com.carpentersblocks.block.safe;

import net.minecraft.util.StringRepresentable;

/** What hoppers and pipes may do with a safe's contents, in the order the hammer cycles them (the original's 0 to 3). */
public enum SafeAutomation implements StringRepresentable {
    DISABLED("disabled", "automation_disabled", false, false),
    SEND("send", "automation_extract", false, true),
    RECEIVE("receive", "automation_insert", true, false),
    ALL("all", "automation_all", true, true);

    private final String name;
    private final String message;
    private final boolean insertion;
    private final boolean extraction;

    SafeAutomation(String name, String message, boolean insertion, boolean extraction) {
        this.name = name;
        this.message = message;
        this.insertion = insertion;
        this.extraction = extraction;
    }

    public boolean allowsInsertion() {
        return insertion;
    }

    public boolean allowsExtraction() {
        return extraction;
    }

    /** The key, under {@code message.carpentersblocks.}, of the line telling the player the new setting. */
    public String message() {
        return message;
    }

    public SafeAutomation next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static SafeAutomation byName(String name) {
        for (SafeAutomation automation : values()) {
            if (automation.name.equals(name)) {
                return automation;
            }
        }
        return DISABLED;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
