package me.exrates.externalservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ChunkUtil {

    private static final String DEFAULT_SEPARATOR = "\n";

    public static String split(List<String> values) throws IOException {
        return split(values, DEFAULT_SEPARATOR);
    }

    public static String split(List<String> values, String separator) throws IOException {
        String result = StringUtils.EMPTY;
        for (String value : values) {
            result += split(value, value.length());
            result += separator;
        }
        return result;
    }

    private static String split(String value, int length) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(value.getBytes());
        byte[] buffer = new byte[length];
        String result = StringUtils.EMPTY;
        while (bis.read(buffer) > 0) {
            for (byte b : buffer) {
                result += (char) b;
            }
        }
        return result;
    }
}