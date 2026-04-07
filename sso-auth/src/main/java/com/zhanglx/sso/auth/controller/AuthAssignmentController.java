package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhanglx.sso.auth.annotation.RepeatSubmit;
import com.zhanglx.sso.auth.domain.dto.AppDTO;
import com.zhanglx.sso.auth.domain.dto.DeptDTO;
import com.zhanglx.sso.auth.domain.dto.PostDTO;
import com.zhanglx.sso.auth.service.AppService;
import com.zhanglx.sso.auth.service.DeptService;
import com.zhanglx.sso.auth.service.PostService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "关系绑定", description = "B 端用户与角色绑定关系维护接口")
@RequestMapping("/apis/v1/auth/s/bindings")
public class AuthAssignmentController {

    private final AppService appService;
    private final DeptService deptService;
    private final PostService postService;

    @Operation(summary = "查询用户已绑定应用")
    @GetMapping("/users/{userId}/apps")
    @SaCheckPermission("user:assign-app")
    public List<AppDTO> listUserApps(@PathVariable String userId) {
        return appService.listByUser(RequestIdUtils.parseId(userId, "用户ID"));
    }

    @Operation(summary = "绑定用户应用")
    @PutMapping("/users/{userId}/apps")
    @RepeatSubmit
    @SaCheckPermission("user:assign-app")
    public List<AppDTO> bindUserApps(@PathVariable String userId, @RequestBody List<String> appCodes) {
        return appService.bindUserApps(RequestIdUtils.parseId(userId, "用户ID"), appCodes);
    }

    @Operation(summary = "查询用户已绑定岗位")
    @GetMapping("/users/{userId}/posts")
    @SaCheckPermission("user:assign-post")
    public List<PostDTO> listUserPosts(@PathVariable String userId) {
        return postService.listByUser(RequestIdUtils.parseId(userId, "用户ID"));
    }

    @Operation(summary = "绑定用户岗位")
    @PutMapping("/users/{userId}/posts")
    @RepeatSubmit
    @SaCheckPermission("user:assign-post")
    public List<PostDTO> bindUserPosts(@PathVariable String userId, @RequestBody List<String> postIds) {
        return postService.bindUserPosts(
                RequestIdUtils.parseId(userId, "用户ID"),
                RequestIdUtils.parseIds(postIds, "岗位ID")
        );
    }

    @Operation(summary = "查询角色已绑定部门")
    @GetMapping("/roles/{roleId}/depts")
    @SaCheckPermission("role:assign-dept")
    public List<DeptDTO> listRoleDepts(@PathVariable String roleId) {
        return deptService.listByRole(RequestIdUtils.parseId(roleId, "角色ID"));
    }

    @Operation(summary = "绑定角色数据范围部门")
    @PutMapping("/roles/{roleId}/depts")
    @RepeatSubmit
    @SaCheckPermission("role:assign-dept")
    public List<DeptDTO> bindRoleDepts(@PathVariable String roleId, @RequestBody List<String> deptIds) {
        return deptService.bindRoleDepts(
                RequestIdUtils.parseId(roleId, "角色ID"),
                RequestIdUtils.parseIds(deptIds, "部门ID")
        );
    }
}
