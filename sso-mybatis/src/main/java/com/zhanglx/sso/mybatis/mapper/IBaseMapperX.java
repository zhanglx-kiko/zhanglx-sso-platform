package com.zhanglx.sso.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.zhanglx.sso.core.domain.page.PageQuery;
import com.zhanglx.sso.core.utils.collection.CollectionUtils;
import com.zhanglx.sso.mybatis.domain.po.BasePO;
import com.zhanglx.sso.mybatis.utils.MyBatisUtils;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/5 17:05
 * @ClassName: IBaseMapperX
 * @Description: 顶层通用 Mapper 扩展
 * <p>
 * 泛型 T 必须继承自 BasePO，以确保拥有 getId() 和 getDelFlag()
 */
public interface IBaseMapperX<T extends BasePO> extends BaseMapper<T> {

    default IPage<T> selectPage(PageQuery pageParam, @Param("ew") Wrapper<T> queryWrapper) {
        // MyBatis Plus 查询
        IPage<T> mpPage = MyBatisUtils.buildPage(pageParam);
        selectPage(mpPage, queryWrapper);
        return mpPage;
    }

    default T selectOne(String field, Object value) {
        return selectOne(new QueryWrapper<T>().eq(field, value));
    }

    default T selectOne(SFunction<T, ?> field, Object value) {
        return selectOne(new LambdaQueryWrapper<T>().eq(field, value));
    }

    default T selectOne(String field1, Object value1, String field2, Object value2) {
        return selectOne(new QueryWrapper<T>().eq(field1, value1).eq(field2, value2));
    }

    default T selectOne(SFunction<T, ?> field1, Object value1, SFunction<T, ?> field2, Object value2) {
        return selectOne(new LambdaQueryWrapper<T>().eq(field1, value1).eq(field2, value2));
    }

    default Long selectCount() {
        return selectCount(new QueryWrapper<T>());
    }

    default Long selectCount(String field, Object value) {
        return selectCount(new QueryWrapper<T>().eq(field, value));
    }

    default Long selectCount(SFunction<T, ?> field, Object value) {
        return selectCount(new LambdaQueryWrapper<T>().eq(field, value));
    }

    default List<T> selectList() {
        return selectList(new QueryWrapper<>());
    }

    default List<T> selectList(String field, Object value) {
        return selectList(new QueryWrapper<T>().eq(field, value));
    }

    default List<T> selectList(SFunction<T, ?> field, Object value) {
        return selectList(new LambdaQueryWrapper<T>().eq(field, value));
    }

    default List<T> selectList(String field, Collection<?> values) {
        return selectList(new QueryWrapper<T>().in(field, values));
    }

    default List<T> selectList(SFunction<T, ?> field, Collection<?> values) {
        return selectList(new LambdaQueryWrapper<T>().in(field, values));
    }

    default void updateBatch(T update) {
        update(update, new QueryWrapper<>());
    }

    /**
     * 逻辑删除：将 del_flag 更新为当前行的 id
     * * @param id 要删除的主键 ID
     *
     * @return 影响行数
     */
    default int deleteByIdWithFill(Long id) {
        if (id == null) {
            return 0;
        }
        // 生成 SQL: UPDATE table SET del_flag = id, update_time = now() WHERE id = id AND del_flag = 0
        return this.update(null, Wrappers.<T>lambdaUpdate()
                .set(T::getDelFlag, id)
                .eq(T::getId, id));
    }

    /**
     * 批量逻辑删除：将多个记录的 del_flag 一次性分别更新为它们各自的 id
     *
     * @param idList 要删除的主键 ID 集合
     * @return 影响行数
     */
    default int deleteByIdsWithFill(Collection<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return 0;
        }
        // 利用 setSql("del_flag = id") 直接下推给 MySQL 执行，无需在 Java 内存中组装实体对象
        // 生成的 SQL: UPDATE table SET del_flag = id, update_time = now() WHERE id IN (...) AND del_flag = 0
        return this.update(null, Wrappers.<T>lambdaUpdate()
                .setSql("del_flag = id")
                .in(T::getId, idList));
    }

    @Override
    default int deleteById(Serializable id) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 deleteById(id)！请使用 deleteByIdWithFill()。");
    }

    // 【新增拦截】拦截 MP 3.5.7+ 新增的 useFill 签名
    @Override
    default int deleteById(Object obj, boolean useFill) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 deleteById(obj, useFill)！请使用 deleteByIdWithFill()。");
    }

    @Override
    default int deleteById(T entity) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 deleteById(entity)！请使用 deleteByIdWithFill()。");
    }

    // 【新增拦截】拦截 MP 3.5.7+ 新增的 deleteByIds 签名
    @Override
    default int deleteByIds(Collection<?> idList) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 deleteByIds！请使用 deleteByIdsWithFill() 实现批量优雅软删除。");
    }

    // 【新增拦截】拦截 MP 3.5.7+ 新增的 deleteByIds 带有 useFill 的签名
    @Override
    default int deleteByIds(Collection<?> collections, boolean useFill) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 deleteByIds(collections, useFill)！请使用 deleteByIdsWithFill()。");
    }

    @Override
    @Deprecated
    default int deleteBatchIds(Collection<?> idList) {
        throw new UnsupportedOperationException("【架构规范】deleteBatchIds 已废弃且被禁用！请使用 deleteByIdsWithFill()。");
    }

    @Override
    default int deleteByMap(Map<String, Object> columnMap) {
        throw new UnsupportedOperationException("【架构规范】高危操作：禁止使用 deleteByMap！");
    }

    @Override
    default int delete(Wrapper<T> queryWrapper) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 delete(Wrapper)！复杂删除请自定义 SQL 或 UpdateWrapper 结合 del_flag=id。");
    }

}
