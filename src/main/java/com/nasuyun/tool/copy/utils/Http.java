package com.nasuyun.tool.copy.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Http {

    private final String endpoint;
    private final String username;
    private final String password;

    public Http(String endpoint, String username, String password) {
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
    }

    public String get(String path) {
        return get(path, null);
    }

    public String get(String path, Map<String, String> params) {
        AtomicReference<String> response = new AtomicReference<>(null);
        Consumer<HttpEntity> consumer = httpEntity -> {
            try {
                response.set(EntityUtils.toString(httpEntity, UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        onGet(consumer, path, params);
        return response.get();
    }

    void onGet(Consumer<HttpEntity> inputStreamConsumer, String path, Map<String, String> params, Header... headers) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            URIBuilder uri = new URIBuilder(url(path));
            if (params != null && params.isEmpty() == false) {
                params.forEach((k, v) -> {
                    uri.setParameter(k, v);
                });
            }
            HttpGet httpGet = new HttpGet(uri.build());
            httpGet.addHeader(header());
            httpGet.addHeader("content-type", "application/json");
            if (headers != null && headers.length > 0) {
                for (Header header : headers) {
                    httpGet.addHeader(header);
                }
            }
            CloseableHttpResponse response = httpclient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode <= 300) {
                inputStreamConsumer.accept(response.getEntity());
            } else {
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity(), UTF_8));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HttpResponse getResponse(String path, Map<String, String> params, Header... headers) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            URIBuilder uri = new URIBuilder(url(path));
            if (params != null && params.isEmpty() == false) {
                params.forEach((k, v) -> {
                    uri.setParameter(k, v);
                });
            }
            HttpGet httpGet = new HttpGet(uri.build());
            httpGet.addHeader(header());
            httpGet.addHeader("content-type", "application/json");
            if (headers != null && headers.length > 0) {
                for (Header header : headers) {
                    httpGet.addHeader(header);
                }
            }
            CloseableHttpResponse response = httpclient.execute(httpGet);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String post(String url, String content) {
        return post(url, null, content);
    }

    public String post(String path, Map<String, String> params, String content) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            URIBuilder uri = new URIBuilder(url(path));
            if (params != null && params.isEmpty() == false) {
                params.forEach((k, v) -> uri.setParameter(k, v));
            }
            HttpPost httpPost = new HttpPost(uri.build());
            httpPost.addHeader(header());
            httpPost.addHeader("content-type", "application/json");
            if (Strings.isNotEmpty(content)) {
                httpPost.setEntity(new StringEntity(content));
            }
            CloseableHttpResponse response = httpclient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode <= 300) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            } else {
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity(), UTF_8));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String put(String url, String content) {
        return put(url, null, content);
    }

    public String put(String path, Map<String, String> params, String content) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            URIBuilder uri = new URIBuilder(url(path));
            if (params != null && params.isEmpty() == false) {
                params.forEach((k, v) -> uri.setParameter(k, v));
            }
            HttpPut httpPut = new HttpPut(uri.build());
            httpPut.addHeader(header());
            httpPut.addHeader("content-type", "application/json");
            if (Strings.isNotEmpty(content)) {
                httpPut.setEntity(new StringEntity(content));
            }
            CloseableHttpResponse response = httpclient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode <= 300) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            } else {
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity(), UTF_8));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String delete(String path) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            URIBuilder uri = new URIBuilder(url(path));
            HttpDelete httpDelete = new HttpDelete(uri.build());
            httpDelete.addHeader(header());
            httpDelete.addHeader("content-type", "application/json");
            CloseableHttpResponse response = httpclient.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode <= 300) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            } else {
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity(), UTF_8));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String url(String path) {
        return endpoint + (path.startsWith("/") ? path : "/" + path);
    }

    private Header header() {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        return new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader);
    }
}
