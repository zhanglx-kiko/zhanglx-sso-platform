package com.zhanglx.sso.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.PostDTO;
import com.zhanglx.sso.auth.domain.dto.PostQueryDTO;
import com.zhanglx.sso.auth.domain.po.PostPO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.domain.po.UserPostPO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.mapper.PostMapper;
import com.zhanglx.sso.auth.mapper.UserMapper;
import com.zhanglx.sso.auth.mapper.UserPostMapper;
import com.zhanglx.sso.auth.service.PostService;
import com.zhanglx.sso.auth.utils.ISystemManageMapper;
import com.zhanglx.sso.core.exception.CommonErrorCode;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 岗位服务实现。
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    /**
     * postMapper。
     */
    private final PostMapper postMapper;
    /**
     * 用户映射器。
     */
    private final UserMapper userMapper;
    /**
     * 用户岗位映射器。
     */
    private final UserPostMapper userPostMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDTO create(PostDTO postDTO) {
        validateCodeUnique(postDTO.getPostCode(), null);
        validateNameUnique(postDTO.getPostName(), null);

        PostPO po = ISystemManageMapper.INSTANCE.toPO(postDTO);
        if (po.getSortNum() == null) {
            po.setSortNum(0);
        }
        if (po.getStatus() == null) {
            po.setStatus(EnableStatusEnum.ENABLED);
        }
        postMapper.insert(po);
        return ISystemManageMapper.INSTANCE.toDTO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDTO update(Long id, PostDTO postDTO) {
        PostPO exist = getPostOrThrow(id);
        validateCodeUnique(postDTO.getPostCode(), id);
        validateNameUnique(postDTO.getPostName(), id);

        exist.setPostCode(postDTO.getPostCode());
        exist.setPostName(postDTO.getPostName());
        exist.setSortNum(postDTO.getSortNum());
        exist.setStatus(postDTO.getStatus());
        PostPO updatePO = new PostPO();
        updatePO.setId(id);
        updatePO.setPostCode(postDTO.getPostCode());
        updatePO.setPostName(postDTO.getPostName());
        updatePO.setSortNum(postDTO.getSortNum());
        updatePO.setStatus(postDTO.getStatus());
        postMapper.updateById(updatePO);
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PostPO exist = getPostOrThrow(id);
        AssertUtils.isTrue(userPostMapper.countByPostId(exist.getId()) == 0, "current post is still assigned to users");
        postMapper.deleteByIdWithFill(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        AssertUtils.notEmpty(ids, "post ids cannot be empty");
        ids.stream().filter(Objects::nonNull).distinct().forEach(this::delete);
    }

    @Override
    public PostDTO getById(Long id) {
        return ISystemManageMapper.INSTANCE.toDTO(getPostOrThrow(id));
    }

    @Override
    public Page<PostDTO> pageQuery(PostQueryDTO queryDTO) {
        Page<PostPO> page = Page.of(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapperX<PostPO> wrapper = new LambdaQueryWrapperX<PostPO>()
                .likeIfPresent(PostPO::getPostCode, queryDTO.getPostCode())
                .likeIfPresent(PostPO::getPostName, queryDTO.getPostName())
                .eqIfPresent(PostPO::getStatus, queryDTO.getStatus())
                .orderByDesc(PostPO::getCreateTime);

        if (StrUtil.isNotBlank(queryDTO.getSearchKey())) {
            wrapper.and(w -> w.like(PostPO::getPostCode, queryDTO.getSearchKey())
                    .or()
                    .like(PostPO::getPostName, queryDTO.getSearchKey()));
        }

        postMapper.selectPage(page, wrapper);
        return buildPage(page, ISystemManageMapper.INSTANCE.toPostDTOList(page.getRecords()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDTO updateStatus(Long id, EnableStatusEnum status) {
        PostPO exist = getPostOrThrow(id);
        exist.setStatus(status);
        PostPO updatePO = new PostPO();
        updatePO.setId(id);
        updatePO.setStatus(status);
        postMapper.updateById(updatePO);
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    public List<PostDTO> listByUser(Long userId) {
        AssertUtils.notNull(userId, "user id cannot be null");
        List<Long> postIds = userPostMapper.selectPostIdsByUserId(userId);
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        List<PostPO> posts = postMapper.selectByIds(postIds);
        return ISystemManageMapper.INSTANCE.toPostDTOList(posts);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PostDTO> bindUserPosts(Long userId, List<Long> postIds) {
        UserPO user = userMapper.selectById(userId);
        AssertUtils.notNull(user, "user not found");

        List<Long> normalizedIds = normalizeIds(postIds);
        if (normalizedIds.isEmpty()) {
            userPostMapper.deleteByUserId(userId);
            return List.of();
        }

        List<PostPO> posts = postMapper.selectByIds(normalizedIds);
        AssertUtils.isTrue(posts.size() == normalizedIds.size(), "invalid post id exists");
        posts.forEach(post -> AssertUtils.isTrue(EnableStatusEnum.isEnabled(post.getStatus()), "disabled post cannot be assigned"));

        Set<Long> target = new LinkedHashSet<>(normalizedIds);
        Set<Long> current = new LinkedHashSet<>(Optional.ofNullable(userPostMapper.selectPostIdsByUserId(userId)).orElse(List.of()));

        List<Long> toDelete = current.stream().filter(id -> !target.contains(id)).toList();
        if (!toDelete.isEmpty()) {
            userPostMapper.deleteByUserIdAndPostIds(userId, toDelete);
        }

        Map<Long, PostPO> postMap = posts.stream().collect(Collectors.toMap(PostPO::getId, Function.identity()));
        for (Long postId : target) {
            if (!current.contains(postId)) {
                userPostMapper.insert(UserPostPO.builder().userId(userId).postId(postId).build());
            }
        }

        return target.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .map(ISystemManageMapper.INSTANCE::toDTO)
                .toList();
    }

    /**
     * 根据标识查询目标数据，不存在时抛出异常。
     */
    private PostPO getPostOrThrow(Long id) {
        AssertUtils.notNull(id, "post id cannot be null");
        PostPO exist = postMapper.selectById(id);
        AssertUtils.notNull(exist, CommonErrorCode.NOT_FOUND);
        return exist;
    }

    /**
     * 校验编码是否唯一。
     */
    private void validateCodeUnique(String postCode, Long excludeId) {
        AssertUtils.notBlank(postCode, "post code cannot be blank");
        LambdaQueryWrapperX<PostPO> wrapper = new LambdaQueryWrapperX<PostPO>().eq(PostPO::getPostCode, postCode);
        if (excludeId != null) {
            wrapper.ne(PostPO::getId, excludeId);
        }
        AssertUtils.isTrue(postMapper.selectCount(wrapper) == 0, "post code already exists");
    }

    /**
     * 校验名称是否唯一。
     */
    private void validateNameUnique(String postName, Long excludeId) {
        AssertUtils.notBlank(postName, "post name cannot be blank");
        LambdaQueryWrapperX<PostPO> wrapper = new LambdaQueryWrapperX<PostPO>().eq(PostPO::getPostName, postName);
        if (excludeId != null) {
            wrapper.ne(PostPO::getId, excludeId);
        }
        AssertUtils.isTrue(postMapper.selectCount(wrapper) == 0, "post name already exists");
    }

    /**
     * 规范化标识集合并去重。
     */
    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    /**
     * 构建分页返回结果。
     */
    private Page<PostDTO> buildPage(Page<PostPO> source, List<PostDTO> records) {
        Page<PostDTO> page = new Page<>();
        page.setCurrent(source.getCurrent());
        page.setSize(source.getSize());
        page.setTotal(source.getTotal());
        page.setRecords(records);
        return page;
    }
}