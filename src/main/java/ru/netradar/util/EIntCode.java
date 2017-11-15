package ru.netradar.util;

import javax.annotation.Nonnull;

/**
 * Created by rfk on 16.11.2017.
 */
@FunctionalInterface
public interface EIntCode {
    @Nonnull
    Integer getCode();
}
