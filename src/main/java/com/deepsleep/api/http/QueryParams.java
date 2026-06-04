package com.deepsleep.api.http;

import okhttp3.HttpUrl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class QueryParams {

    private static final QueryParams EMPTY = new QueryParams(Map.of());

    private final Map<String, String> values;

    private QueryParams(Map<String, String> values) {
        this.values = values;
    }

    public static QueryParams empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public HttpUrl applyTo(HttpUrl url) {
        if (values.isEmpty()) {
            return url;
        }
        HttpUrl.Builder builder = url.newBuilder();
        values.forEach(builder::addQueryParameter);
        return builder.build();
    }

    public static final class Builder {
        private final Map<String, String> values = new LinkedHashMap<>();

        public Builder add(String name, Object value) {
            if (value == null) {
                return this;
            }
            if (value instanceof String string && string.isBlank()) {
                return this;
            }
            values.put(name, stringify(value));
            return this;
        }

        public QueryParams build() {
            if (values.isEmpty()) {
                return QueryParams.empty();
            }
            return new QueryParams(Map.copyOf(values));
        }

        private static String stringify(Object value) {
            Objects.requireNonNull(value, "value");
            if (value instanceof LocalDate localDate) {
                return localDate.toString();
            }
            if (value instanceof LocalDateTime localDateTime) {
                return localDateTime.toString();
            }
            return String.valueOf(value);
        }
    }
}
