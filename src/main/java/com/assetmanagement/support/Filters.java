package com.assetmanagement.support;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Shared in-memory filtering helpers used by the history/list endpoints.
 * Base scoping and equipment-type filtering are applied before date filtering so
 * that every module reports data the same way.
 */
public final class Filters {
    private Filters() {
    }

    public static <T> List<T> byBaseAndType(
            List<T> rows,
            Long baseId,
            String equipmentType,
            Function<T, Long> baseAccessor,
            Function<T, String> typeAccessor
    ) {
        String type = normalizeType(equipmentType);
        return rows.stream()
                .filter(row -> baseId == null || baseId.equals(baseAccessor.apply(row)))
                .filter(row -> type == null || type.equalsIgnoreCase(typeAccessor.apply(row)))
                .toList();
    }

    public static <T> List<T> byDateRange(
            List<T> rows,
            LocalDate startDate,
            LocalDate endDate,
            Function<T, LocalDate> dateAccessor
    ) {
        return rows.stream()
                .filter(row -> {
                    LocalDate date = dateAccessor.apply(row);
                    if (date == null) {
                        return false;
                    }
                    if (startDate != null && date.isBefore(startDate)) {
                        return false;
                    }
                    return endDate == null || !date.isAfter(endDate);
                })
                .toList();
    }

    public static <T> List<T> byInstantRange(
            List<T> rows,
            LocalDate startDate,
            LocalDate endDate,
            Function<T, Instant> instantAccessor
    ) {
        return byDateRange(rows, startDate, endDate, row -> toUtcDate(instantAccessor.apply(row)));
    }

    public static String normalizeType(String equipmentType) {
        if (equipmentType == null || equipmentType.isBlank() || "ALL".equalsIgnoreCase(equipmentType)) {
            return null;
        }
        return equipmentType.toUpperCase(Locale.ROOT);
    }

    private static LocalDate toUtcDate(Instant instant) {
        return instant == null ? null : instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
