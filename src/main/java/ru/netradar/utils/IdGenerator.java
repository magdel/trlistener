package ru.netradar.utils;

import javax.annotation.Nonnull;
import java.util.List;

public interface IdGenerator {

    @Nonnull
    Long generate();

    @Nonnull
    List<Long> generateBatch(int size);

}