package com.example.demo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import weaver.rsa.security.RSA;

import javax.net.ssl.SSLContext;
import java.util.*;


public class OATokenUtils {

    public static final String APP_ID = "BD0ECA99-6E4B-7CA0-3B33-30BFC009958E";
    public static final String IP = "http://10.10.10.15";
    public String spk;
    public RSA rsa;

    /**
     * 注册方法
     *
     * @throws Exception 可能抛出的异常
     */
    private static String register() throws Exception {
        Map<String, String> headers = new HashMap<>();

        String cpk = RSA.getRSA_PUB();
        headers.put("appid", APP_ID);
        headers.put("cpk", cpk);

        return HttpManager.postDataSSL(IP + "/api/ec/dev/auth/regist", null, headers);
    }

    public String getToken() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap registerResponse = objectMapper.readValue(register(), HashMap.class);
        this.rsa = new RSA();
        Map<String, String> headers = new HashMap<>();
        this.spk = registerResponse.get("spk").toString();
        String secret = rsa.encrypt(null, registerResponse.get("secret").toString(), null, "utf-8", spk, false);
        headers.put("appid", APP_ID);
        headers.put("secret", secret);
        String body = HttpManager.postDataSSL(IP + "/api/ec/dev/auth/applytoken", null, headers);
        return objectMapper.readValue(body, Map.class).get("token").toString();
    }


    // HttpManager
    public static class HttpManager {

        private static final CloseableHttpClient httpClient = createSSLClient();

        public static String postDataSSL(String url, Map<String, Object> body, Map<String, String> headers) throws Exception {
            HttpPost httpPost = new HttpPost(url);

            // 设置请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // 设置请求体
            List<NameValuePair> params = new ArrayList<>();
            if (body != null) {
                Set<String> keys = body.keySet();
                for (String key : keys) {
                    params.add(new BasicNameValuePair(key, String.valueOf(body.get(key))));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
            }

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity, "UTF-8");
                }
                return null;
            } finally {
                httpPost.releaseConnection();
            }
        }

        private static CloseableHttpClient createSSLClient() {
            try {
                SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
                return HttpClients.custom().setSSLSocketFactory(sslsf).build();
            } catch (Exception e) {
                throw new RuntimeException("SSL配置失败", e);
            }
        }
    }


}
