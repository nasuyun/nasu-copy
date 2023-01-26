package com.nasuyun.tool.copy.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.security.auth.login.CredentialExpiredException;
import java.util.Map;

@Component
public class SessionProvider {

    /**
     * 600分钟没操作会话过期
     */
    @Value("${session.expire.seconds:36000}")
    private int expireSeconds;

    /**
     * key : sessionKey
     * value : UserCredential
     */
    private Map<String, UserCredential> container;

    @PostConstruct
    void onCreate() {
        PassiveExpiringMap.ExpirationPolicy<String, UserCredential> policy = new PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<>(expireSeconds * 1000);
        container = new PassiveExpiringMap<>(policy);
    }

    UserCredential createSession(String username, String authority) {
        return createSession(RandomStringUtils.randomAlphabetic(26), username, authority);
    }

    /**
     * 颁发token
     * @return
     */
    UserCredential createSession(String sessionKey, String username, String authority) {
        UserCredential userCredential = new UserCredential(sessionKey, username, authority);
        container.put(sessionKey, userCredential);
        return userCredential;
    }

    /**
     * filter 内设置前端传过来的用户信息
     *
     * @param sessionKey
     * @param username
     * @param authority
     * @throws CredentialExpiredException
     */
    void setUserCredential(String sessionKey, String username, String authority) throws CredentialExpiredException {
        if (container.containsKey(sessionKey) == false) {
            throw new CredentialExpiredException("会话失效，请重新登录");
        }
        container.put(sessionKey, new UserCredential(sessionKey, username, authority));
        sessionKeyHolder.set(sessionKey);
    }

    public UserCredential get(String sessionKey) {
        return container.get(sessionKey);
    }

    /**
     * 获取用户
     *
     * @return UserCredential
     */
    public UserCredential current() {
        return container.get(sessionKeyHolder.get());
    }

    void clearSession() {
        sessionKeyHolder.set(null);
    }

    @Getter
    @AllArgsConstructor
    public static class UserCredential {
        String sessionKey;
        String username;
        String authority;
    }

    static ThreadLocal<String> sessionKeyHolder = new ThreadLocal<>();
}
