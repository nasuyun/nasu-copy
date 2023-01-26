package com.nasuyun.tool.copy.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectInfo {
    private String endpoint;
    private String username;
    private String password;
}
