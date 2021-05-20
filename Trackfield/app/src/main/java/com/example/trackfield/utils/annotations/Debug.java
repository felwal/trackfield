package com.example.trackfield.utils.annotations;

import com.example.trackfield.data.prefs.Prefs;

/**
 * Should not be called during production, unless checking {@link Prefs#isDeveloper()} first.
 */
public @interface Debug {
}
