package com.nasuyun.tool.copy.core.model.ext;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Field {
    String type;
    boolean docvalue;
    boolean store;
}
