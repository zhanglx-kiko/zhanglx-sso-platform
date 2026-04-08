package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.PostDTO;
import com.zhanglx.sso.auth.domain.dto.PostQueryDTO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;

import java.util.List;

public interface PostService {

    PostDTO create(PostDTO postDTO);

    PostDTO update(Long id, PostDTO postDTO);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    PostDTO getById(Long id);

    Page<PostDTO> pageQuery(PostQueryDTO queryDTO);

    PostDTO updateStatus(Long id, EnableStatusEnum status);

    List<PostDTO> listByUser(Long userId);

    List<PostDTO> bindUserPosts(Long userId, List<Long> postIds);
}
