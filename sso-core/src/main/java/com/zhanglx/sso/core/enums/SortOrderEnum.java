package com.zhanglx.sso.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/5 16:27
 * 类名：SortOrderEnum
 * 说明：
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SortOrderEnum implements IStringBaseEnum<String> {
    ASC("asc", "升序"),
    DESC("desc", "降序"),
    ;

    /**
     * code
     */
    @JsonValue
    private String code;

    /**
     * description
     */
    private String description;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<String> codeValues() {
        return Arrays.stream(values()).map(SortOrderEnum::getCode).collect(Collectors.toList());
    }
}