package com.felwal.trackfield.utils.annotation;

import com.felwal.trackfield.data.prefs.Prefs;

/**
 * Should not be called during production, unless checking {@link Prefs#isDeveloper()} first.
 */
public @interface Debug {
}
