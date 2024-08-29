package net.vitacraft.api.info;

/**
 * The {@code StartUpPriority} enum represents different levels of startup priority
 * that a module can have in the system. This priority indicates the order in which
 * modules should be enabled during the startup sequence.
 * <p>
 * The priorities are arranged from {@code VERY_HIGH} to {@code VERY_LOW}.
 * The priority of a module should only be modified if required for it to work properly.
 * </p>
 *
 */
public enum StartUpPriority {
    VERY_HIGH,
    HIGH,
    DEFAULT,
    LOW,
    VERY_LOW
}
