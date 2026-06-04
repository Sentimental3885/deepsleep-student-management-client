package com.deepsleep.api.http;

import com.fasterxml.jackson.core.type.TypeReference;

public record ApiRequest<T>(
        String method,
        String uri,
        T body,
        QueryParams queryParams
) {

    public static ApiRequest<Void> get(String uri) {
        return get(uri, QueryParams.empty());
    }

    public static ApiRequest<Void> get(String uri, QueryParams queryParams) {
        return new ApiRequest<>("GET", uri, null, queryParams);
    }

    public static <T> ApiRequest<T> post(String uri, T body) {
        return new ApiRequest<>("POST", uri, body, QueryParams.empty());
    }

    public static <T> ApiRequest<T> put(String uri, T body) {
        return new ApiRequest<>("PUT", uri, body, QueryParams.empty());
    }

    public static ApiRequest<Void> delete(String uri) {
        return new ApiRequest<>("DELETE", uri, null, QueryParams.empty());
    }

    public static <R> TypeReference<R> type(TypeReference<R> typeReference) {
        return typeReference;
    }
}
