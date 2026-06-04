package com.deepsleep.api.http;

import com.deepsleep.api.config.ApiConfig;
import com.deepsleep.api.result.ApiException;
import com.deepsleep.api.result.ClientException;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.result.ResultCode;
import com.deepsleep.auth.TokenStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType OCTET_STREAM = MediaType.get("application/octet-stream");

    private final String baseUrl;
    private final TokenStore tokenStore;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public ApiClient(String baseUrl, TokenStore tokenStore) {
        this(ApiConfig.of(baseUrl), tokenStore);
    }

    public ApiClient(ApiConfig config, TokenStore tokenStore) {
        this.baseUrl = normalizeBaseUrl(config.baseUrl());
        this.tokenStore = tokenStore;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.client = new OkHttpClient.Builder()
                .connectTimeout(config.connectTimeout())
                .readTimeout(config.readTimeout())
                .writeTimeout(config.writeTimeout())
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = tokenStore.getAccessToken();

                    Request.Builder builder = original.newBuilder();

                    if (token != null && !token.isBlank()) {
                        builder.header("Authorization", "Bearer " + token);
                    }

                    return chain.proceed(builder.build());
                })
                .build();
    }

    public <T> CompletableFuture<T> get(String path, TypeReference<Result<T>> responseType) {
        return get(path, QueryParams.empty(), responseType);
    }

    public <T> CompletableFuture<T> get(String path, QueryParams queryParams, TypeReference<Result<T>> responseType) {
        return send(ApiRequest.get(path, queryParams), responseType);
    }

    public <T> CompletableFuture<T> post(String path, Object body, TypeReference<Result<T>> responseType) {
        return send(ApiRequest.post(path, body), responseType);
    }

    public CompletableFuture<Void> postVoid(String path, Object body) {
        return post(path, body, new TypeReference<>() {});
    }

    public <T> CompletableFuture<T> put(String path, Object body, TypeReference<Result<T>> responseType) {
        return send(ApiRequest.put(path, body), responseType);
    }

    public CompletableFuture<Void> putVoid(String path, Object body) {
        return put(path, body, new TypeReference<>() {});
    }

    public CompletableFuture<Void> delete(String path) {
        return send(ApiRequest.delete(path), new TypeReference<>() {});
    }

    public <T> CompletableFuture<T> send(ApiRequest<?> apiRequest, TypeReference<Result<T>> responseType) {
        return CompletableFuture.supplyAsync(() -> execute(apiRequest, responseType));
    }

    public <T> CompletableFuture<T> upload(
            String path,
            String formFieldName,
            Path file,
            TypeReference<Result<T>> responseType
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RequestBody fileBody = RequestBody.create(Files.readAllBytes(file), OCTET_STREAM);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(formFieldName, file.getFileName().toString(), fileBody)
                        .build();

                Request request = new Request.Builder()
                        .url(resolveUrl(path, QueryParams.empty()))
                        .put(requestBody)
                        .build();
                return execute(request, responseType);
            } catch (IOException ioe) {
                throw new ClientException("读取上传文件失败", ioe);
            }
        });
    }

    private <T> T execute(ApiRequest<?> apiRequest, TypeReference<Result<T>> responseType) {
        Request request = toOkHttpRequest(apiRequest);
        return execute(request, responseType);
    }

    private <T> T execute(Request request, TypeReference<Result<T>> responseType) {
        try (Response response = client.newCall(request).execute()) {
            String responseText = readBody(response.body());
            Result<T> result = parseResult(responseText, responseType, response.code());

            if (!response.isSuccessful() || !result.success()) {
                if (ResultCode.UNAUTHORIZED.matches(result.code()) || response.code() == 401) {
                    tokenStore.clear();
                }
                throw new ApiException(result.code(), messageOf(result), response.code());
            }
            return result.data();
        } catch (JsonProcessingException jpe) {
            throw new ClientException("响应数据解析失败", jpe);
        } catch (IOException ioe) {
            throw new ClientException("网络连接异常", ioe);
        }
    }

    private Request toOkHttpRequest(ApiRequest<?> apiRequest) {
        RequestBody requestBody = bodyOf(apiRequest);
        Request.Builder builder = new Request.Builder()
                .url(resolveUrl(apiRequest.uri(), apiRequest.queryParams()));

        return switch (apiRequest.method().toUpperCase()) {
            case "GET" -> builder.get().build();
            case "POST" -> builder.post(nonNullBody(requestBody)).build();
            case "PUT" -> builder.put(nonNullBody(requestBody)).build();
            case "DELETE" -> requestBody == null ? builder.delete().build() : builder.delete(requestBody).build();
            default -> throw new ClientException("不支持的请求方法：" + apiRequest.method());
        };
    }

    private RequestBody bodyOf(ApiRequest<?> apiRequest) {
        Object body = apiRequest.body();
        if (body == null) {
            return null;
        }
        try {
            return RequestBody.create(objectMapper.writeValueAsString(body), JSON);
        } catch (JsonProcessingException jpe) {
            throw new ClientException("请求数据序列化失败", jpe);
        }
    }

    private RequestBody nonNullBody(RequestBody requestBody) {
        return requestBody == null ? RequestBody.create(new byte[0], JSON) : requestBody;
    }

    private <T> Result<T> parseResult(String responseText, TypeReference<Result<T>> responseType, int httpStatus)
            throws JsonProcessingException {
        if (responseText == null || responseText.isBlank()) {
            if (httpStatus >= 200 && httpStatus < 300) {
                return new Result<>(ResultCode.SUCCESS.getCode(), null, ResultCode.SUCCESS.getMsg());
            }
            return new Result<>(httpStatus, null, "HTTP " + httpStatus);
        }
        return objectMapper.readValue(responseText, responseType);
    }

    private String readBody(ResponseBody responseBody) throws IOException {
        return responseBody == null ? "" : responseBody.string();
    }

    private String messageOf(Result<?> result) {
        if (result.msg() != null && !result.msg().isBlank()) {
            return result.msg();
        }
        return result.resultCode().getMsg();
    }

    private HttpUrl resolveUrl(String path, QueryParams queryParams) {
        String cleanPath = Objects.requireNonNull(path, "path");
        String fullUrl = cleanPath.startsWith("http://") || cleanPath.startsWith("https://")
                ? cleanPath
                : baseUrl + (cleanPath.startsWith("/") ? cleanPath.substring(1) : cleanPath);

        HttpUrl url = HttpUrl.parse(fullUrl);
        if (url == null) {
            throw new ClientException("接口地址无效：" + fullUrl);
        }
        return queryParams.applyTo(url);
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("baseUrl 不能为空");
        }
        return value.endsWith("/") ? value : value + "/";
    }

}
