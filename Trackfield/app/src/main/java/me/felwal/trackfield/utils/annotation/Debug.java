package me.felwal.trackfield.utils.annotation;

import me.felwal.trackfield.data.prefs.Prefs;

/**
 * Should not be called during production, unless checking {@link Prefs#isDeveloper()} first.
 */
public @interface Debug {
}
