package com.zhanglx.sso.horticulturalplants.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum PlantItemPublishStatusEnum implements IIntegerBaseEnum<String> {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "上架"),
    OFF_SHELF(2, "下架");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String description;

    public static PlantItemPublishStatusEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, PlantItemPublishStatusEnum.class);
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof PlantItemPublishStatusEnum statusEnum) {
            return this == statusEnum;
        }
        if (value instanceof Number number) {
            return Objects.equals(code, number.intValue());
        }
        if (value instanceof String text) {
            return Objects.equals(String.valueOf(code), text) || name().equalsIgnoreCase(text);
        }
        return false;
    }
}
