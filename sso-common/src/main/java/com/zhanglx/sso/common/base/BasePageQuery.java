package com.zhanglx.sso.common.base;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:14
 * @ClassName: BasePageQuery
 * @Description: 分页查询参数基类
 */
public class BasePageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Integer pageNum;
    private Integer pageSize;

    public BasePageQuery() {
    }

    public BasePageQuery(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public BasePageQuery setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public BasePageQuery setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BasePageQuery that = (BasePageQuery) o;
        return Objects.equals(pageNum, that.pageNum) && Objects.equals(pageSize, that.pageSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNum, pageSize);
    }

    @Override
    public String toString() {
        return "BasePageQuery{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}
