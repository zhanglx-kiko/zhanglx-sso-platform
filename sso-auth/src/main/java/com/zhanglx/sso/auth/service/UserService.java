package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.UserBaseDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPageQueryDTO;

public interface UserService {

    void addUser(UserDTO user);

    void updateUserInfo(UserBaseDTO userBaseDTO);

    void removeUserById(Long userId);

    Page<UserDTO> pageQuery(UserPageQueryDTO query);

    UserDTO getUserByOpenId(String openId);

    UserDTO addWxUser(UserDTO user, String openId);

    UserDTO findUserByUsername(String username);

    void disableUser(Long userId);
}
