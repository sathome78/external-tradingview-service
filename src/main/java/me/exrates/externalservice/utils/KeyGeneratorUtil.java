package me.exrates.externalservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public final class KeyGeneratorUtil {

    public static String generate(String delimiter, String... params) {
        return String.join(delimiter, params);
    }
}