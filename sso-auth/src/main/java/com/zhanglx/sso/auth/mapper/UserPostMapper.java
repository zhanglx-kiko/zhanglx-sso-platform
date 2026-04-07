package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.UserPostPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserPostMapper extends IBaseMapperX<UserPostPO> {

    @Select("SELECT post_id FROM t_auth_user_post WHERE user_id = #{userId} AND del_flag = 0")
    List<Long> selectPostIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM t_auth_user_post WHERE post_id = #{postId} AND del_flag = 0")
    Long countByPostId(@Param("postId") Long postId);

    @Update("UPDATE t_auth_user_post SET del_flag = id WHERE user_id = #{userId} AND del_flag = 0")
    int deleteByUserId(@Param("userId") Long userId);

    @Update("<script>" +
            "UPDATE t_auth_user_post SET del_flag = id " +
            "WHERE user_id = #{userId} AND del_flag = 0 AND post_id IN " +
            "<foreach item='postId' collection='postIds' open='(' separator=',' close=')'>" +
            "#{postId}" +
            "</foreach>" +
            "</script>")
    int deleteByUserIdAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    @Update("<script>" +
            "UPDATE t_auth_user_post SET del_flag = id " +
            "WHERE user_id IN " +
            "<foreach item='userId' collection='userIds' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach> AND del_flag = 0" +
            "</script>")
    int deleteByUserIds(@Param("userIds") List<Long> userIds);
}
