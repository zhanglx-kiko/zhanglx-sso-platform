package com.zhanglx.sso.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
 * 作者：Zhang L X
 * 创建时间：2026/3/5 17:05
 * 类名：IBaseMapperX
 * 说明：顶层通用 Mapper 扩展
 * <p>
 * 泛型 T 必须继承自 BasePO，以确保拥有主键与逻辑删除字段。
 */
public interface IBaseMapperX<T extends BasePO> extends BaseMapper<T> {

    /**
     * 按项目统一的分页参数执行分页查询。
     *
     * @param pageParam    统一分页参数对象
     * @param queryWrapper 查询条件包装器
     * @return MyBatis-Plus 分页结果对象
     */
    default IPage<T> selectPage(PageQuery pageParam, @Param("ew") Wrapper<T> queryWrapper) {
        IPage<T> mpPage = MyBatisUtils.buildPage(pageParam);
        selectPage(mpPage, queryWrapper);
        return mpPage;
    }

    /**
     * 按字段名和字段值查询单条记录。
     *
     * @param field 数据库字段名
     * @param value 字段值
     * @return 匹配到的单条记录
     */
    default T selectOne(String field, Object value) {
        return selectOne(new QueryWrapper<T>().eq(field, value));
    }

    /**
     * 按 Lambda 字段和字段值查询单条记录。
     *
     * @param field Lambda 字段引用
     * @param value 字段值
     * @return 匹配到的单条记录
     */
    default T selectOne(SFunction<T, ?> field, Object value) {
        return selectOne(new LambdaQueryWrapper<T>().eq(field, value));
    }

    /**
     * 按两个普通字段条件查询单条记录。
     *
     * @param field1 第一个字段名
     * @param value1 第一个字段值
     * @param field2 第二个字段名
     * @param value2 第二个字段值
     * @return 匹配到的单条记录
     */
    default T selectOne(String field1, Object value1, String field2, Object value2) {
        return selectOne(new QueryWrapper<T>().eq(field1, value1).eq(field2, value2));
    }

    /**
     * 按两个 Lambda 字段条件查询单条记录。
     *
     * @param field1 第一个 Lambda 字段引用
     * @param value1 第一个字段值
     * @param field2 第二个 Lambda 字段引用
     * @param value2 第二个字段值
     * @return 匹配到的单条记录
     */
    default T selectOne(SFunction<T, ?> field1, Object value1, SFunction<T, ?> field2, Object value2) {
        return selectOne(new LambdaQueryWrapper<T>().eq(field1, value1).eq(field2, value2));
    }

    /**
     * 查询当前表的有效记录总数。
     *
     * @return 记录总数
     */
    default Long selectCount() {
        return selectCount(new QueryWrapper<T>());
    }

    /**
     * 按字段名和字段值统计记录数量。
     *
     * @param field 数据库字段名
     * @param value 字段值
     * @return 匹配记录数
     */
    default Long selectCount(String field, Object value) {
        return selectCount(new QueryWrapper<T>().eq(field, value));
    }

    /**
     * 按 Lambda 字段和字段值统计记录数量。
     *
     * @param field Lambda 字段引用
     * @param value 字段值
     * @return 匹配记录数
     */
    default Long selectCount(SFunction<T, ?> field, Object value) {
        return selectCount(new LambdaQueryWrapper<T>().eq(field, value));
    }

    /**
     * 查询当前表的全部有效记录。
     *
     * @return 全量记录列表
     */
    default List<T> selectList() {
        return selectList(new QueryWrapper<>());
    }

    /**
     * 按字段名和字段值查询记录列表。
     *
     * @param field 数据库字段名
     * @param value 字段值
     * @return 匹配记录列表
     */
    default List<T> selectList(String field, Object value) {
        return selectList(new QueryWrapper<T>().eq(field, value));
    }

    /**
     * 按 Lambda 字段和字段值查询记录列表。
     *
     * @param field Lambda 字段引用
     * @param value 字段值
     * @return 匹配记录列表
     */
    default List<T> selectList(SFunction<T, ?> field, Object value) {
        return selectList(new LambdaQueryWrapper<T>().eq(field, value));
    }

    /**
     * 按字段名和候选值集合查询记录列表。
     *
     * @param field  数据库字段名
     * @param values 候选值集合
     * @return 匹配记录列表
     */
    default List<T> selectList(String field, Collection<?> values) {
        return selectList(new QueryWrapper<T>().in(field, values));
    }

    /**
     * 按 Lambda 字段和候选值集合查询记录列表。
     *
     * @param field  Lambda 字段引用
     * @param values 候选值集合
     * @return 匹配记录列表
     */
    default List<T> selectList(SFunction<T, ?> field, Collection<?> values) {
        return selectList(new LambdaQueryWrapper<T>().in(field, values));
    }

    /**
     * 按当前查询条件批量更新记录。
     *
     * @param update 待更新的实体对象
     */
    default void updateBatch(T update) {
        update(update, new QueryWrapper<>());
    }

    /**
     * 按主键执行项目约定的逻辑删除。
     *
     * <p>逻辑删除规则为：把 {@code del_flag} 更新为当前行的 {@code id}，从而既保留历史数据，
     * 又能释放唯一索引占位。</p>
     *
     * @param id 需要删除的主键 ID
     * @return 影响行数
     */
    default int deleteByIdWithFill(Long id) {
        if (id == null) {
            return 0;
        }
        return this.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<T>()
                .setSql("del_flag = id")
                .eq("id", id));
    }

    /**
     * 按主键集合执行项目约定的批量逻辑删除。
     *
     * @param idList 需要删除的主键 ID 集合
     * @return 影响行数
     */
    default int deleteByIdsWithFill(Collection<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return 0;
        }
        return this.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<T>()
                .setSql("del_flag = id")
                .in("id", idList));
    }

    /**
     * 禁止直接调用 MyBatis-Plus 原生的物理删除接口。
     *
     * @param id 主键 ID
     * @return 永不返回
     */
    @Override
    default int deleteById(Serializable id) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 deleteById(id)！请使用 deleteByIdWithFill()。");
    }

    /**
     * 禁止调用 MyBatis-Plus 新增的带 useFill 标记的删除接口。
     *
     * @param obj     主键对象或实体对象
     * @param useFill 是否启用填充逻辑
     * @return 永不返回
     */
    @Override
    default int deleteById(Object obj, boolean useFill) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 deleteById(obj, useFill)！请使用 deleteByIdWithFill()。");
    }

    /**
     * 禁止按实体直接调用原生删除接口。
     *
     * @param entity 实体对象
     * @return 永不返回
     */
    @Override
    default int deleteById(T entity) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 deleteById(entity)！请使用 deleteByIdWithFill()。");
    }

    /**
     * 禁止直接调用原生批量删除接口。
     *
     * @param idList 主键集合
     * @return 永不返回
     */
    @Override
    default int deleteByIds(Collection<?> idList) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 deleteByIds！请使用 deleteByIdsWithFill() 实现批量优雅软删除。");
    }

    /**
     * 禁止调用带 useFill 标记的原生批量删除接口。
     *
     * @param collections 主键集合
     * @param useFill     是否启用填充逻辑
     * @return 永不返回
     */
    @Override
    default int deleteByIds(Collection<?> collections, boolean useFill) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 deleteByIds(collections, useFill)！请使用 deleteByIdsWithFill()。");
    }

    /**
     * 禁止继续使用已废弃的 deleteBatchIds 接口。
     *
     * @param idList 主键集合
     * @return 永不返回
     */
    @Override
    @Deprecated
    default int deleteBatchIds(Collection<?> idList) {
        throw new UnsupportedOperationException("【架构规范】deleteBatchIds 已废弃且被禁用！请使用 deleteByIdsWithFill()。");
    }

    /**
     * 禁止通过 map 条件执行删除操作，避免误删。
     *
     * @param columnMap 字段条件集合
     * @return 永不返回
     */
    @Override
    default int deleteByMap(Map<String, Object> columnMap) {
        throw new UnsupportedOperationException("【架构规范】高危操作：禁止使用 deleteByMap！");
    }

    /**
     * 禁止直接按 Wrapper 执行删除，强制使用逻辑删除方案。
     *
     * @param queryWrapper 删除条件
     * @return 永不返回
     */
    @Override
    default int delete(Wrapper<T> queryWrapper) {
        throw new UnsupportedOperationException("【架构规范】禁止调用原生 delete(Wrapper)！复杂删除请自定义 SQL 或 UpdateWrapper 结合 del_flag=id。");
    }

}