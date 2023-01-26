package com.nasuyun.tool.copy.utils;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TemplateFileUtils {

    public static String replace(String fileInClasspath, Map<String, Object> params) {
        return replaceHolder(readFile(fileInClasspath), params);
    }

    private static String readFile(String classpathFile) {
        Resource resource = new ClassPathResource(classpathFile);
        try {
            return FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("read file io error:" + classpathFile, e);
        }
    }

    private static String replaceHolder(String source, Map<String, Object> params) {
        StrSubstitutor sub = new StrSubstitutor(params, "${", "}");
        return sub.replace(source);
    }
}
