package ru.yandex.practicum.filmorate.storage;

import java.util.Collections;

final class SqlUtils {

    private SqlUtils() {
    }

    static String placeholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }
}
