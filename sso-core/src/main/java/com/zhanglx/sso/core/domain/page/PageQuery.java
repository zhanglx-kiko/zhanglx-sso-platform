package com.zhanglx.sso.core.domain.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:14
 * @ClassName: PageQuery
 * @Description: 分页查询参数基类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery implements Serializable {

    /**
     * 最大分页大小限制
     */
    private static final Integer MAX_PAGE_SIZE = 100;
    private static final Integer PAGE_NUM = 1;
    private static final Integer PAGE_SIZE = 10;

    @Serial
    private static final long serialVersionUID = 1L;

    @Min(value = 1, message = "页码必须大于0")
    private final Integer pageNum = PAGE_NUM;

    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private final Integer pageSize = PAGE_SIZE;

    /**
     * 排序条件
     */
    private List<SortingField> sortingFields;

    /**
     * 模糊检索key
     */
    private String searchKey;

}
