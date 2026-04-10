package com.zhanglx.sso.core.domain.page;

import com.zhanglx.sso.core.enums.SortOrderEnum;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/5 17:21
 * 类名：SortingField
 * 说明：
 */
@Data
@Getter
public class SortingField implements Serializable {

    /**
     * 顺序 - 升序
     */
    public static final String ORDER_ASC = "asc";

    /**
     * 顺序 - 降序
     */
    public static final String ORDER_DESC = "desc";


    /**
     * 排序字段
     */
    private String field;

    /**
     * 排序顺序
     */
    private SortOrderEnum order;

}