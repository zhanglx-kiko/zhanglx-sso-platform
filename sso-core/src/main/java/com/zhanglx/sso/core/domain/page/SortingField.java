package com.zhanglx.sso.core.domain.page;

import com.zhanglx.sso.core.enums.SortOrderEnum;
import lombok.Data;import lombok.Getter;

import java.io.Serializable;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/5 17:21
 * @ClassName: SortingField
 * @Description:
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
