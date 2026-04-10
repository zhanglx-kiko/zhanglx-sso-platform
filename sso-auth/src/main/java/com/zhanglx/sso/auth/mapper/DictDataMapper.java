package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.DictDataPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 字典数据数据访问层。
 */
@Mapper
public interface DictDataMapper extends IBaseMapperX<DictDataPO> {

    /**
     * 查询某个字典类型下的第一条字典标签。
     */
    @Select("SELECT dict_label FROM t_sys_dict_data WHERE dict_type = #{dictType} AND del_flag = 0 ORDER BY id ASC LIMIT 1")
    String selectFirstDictLabelByDictType(@Param("dictType") String dictType);
}
