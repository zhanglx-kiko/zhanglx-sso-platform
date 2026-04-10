package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.UserAppPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * UserApp数据访问层。
 */
@Mapper
public interface UserAppMapper extends IBaseMapperX<UserAppPO> {

    /**
     * 根据用户标识查询应用编码列表。
     */
    @Select("SELECT app_code FROM t_auth_user_app WHERE user_id = #{userId} AND del_flag = 0")
    List<String> selectAppCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据应用编码统计用户应用关联数量。
     */
    @Select("SELECT COUNT(1) FROM t_auth_user_app WHERE app_code = #{appCode} AND del_flag = 0")
    Long countByAppCode(@Param("appCode") String appCode);

    /**
     * 根据用户标识逻辑删除应用关联关系。
     */
    @Update("UPDATE t_auth_user_app SET del_flag = id WHERE user_id = #{userId} AND del_flag = 0")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和应用编码列表逻辑删除指定应用关联关系。
     */
    @Update("<script>" +
            "UPDATE t_auth_user_app SET del_flag = id " +
            "WHERE user_id = #{userId} AND del_flag = 0 AND app_code IN " +
            "<foreach item='appCode' collection='appCodes' open='(' separator=',' close=')'>" +
            "#{appCode}" +
            "</foreach>" +
            "</script>")
    int deleteByUserIdAndAppCodes(@Param("userId") Long userId, @Param("appCodes") List<String> appCodes);

    /**
     * 根据用户ID列表批量逻辑删除应用关联关系。
     */
    @Update("<script>" +
            "UPDATE t_auth_user_app SET del_flag = id " +
            "WHERE user_id IN " +
            "<foreach item='userId' collection='userIds' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach> AND del_flag = 0" +
            "</script>")
    int deleteByUserIds(@Param("userIds") List<Long> userIds);
}