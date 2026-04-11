package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.AdminMemberForceLogoutDTO;
import com.zhanglx.sso.auth.domain.dto.AdminMemberQueryDTO;
import com.zhanglx.sso.auth.domain.dto.AdminMemberStatusUpdateDTO;
import com.zhanglx.sso.auth.domain.dto.AuthLoginLogQueryDTO;
import com.zhanglx.sso.auth.domain.vo.AdminMemberDetailVO;
import com.zhanglx.sso.auth.domain.vo.AdminMemberListVO;
import com.zhanglx.sso.auth.domain.vo.MemberLoginAuditVO;
import com.zhanglx.sso.auth.domain.vo.MemberManageRecordVO;
import com.zhanglx.sso.auth.domain.vo.MemberSocialBindingVO;
import com.zhanglx.sso.core.domain.page.PageQuery;

import java.util.List;

/**
 * 会员后台管理服务。
 */
public interface AdminMemberManageService {

    Page<AdminMemberListVO> pageQuery(AdminMemberQueryDTO queryDTO);

    AdminMemberDetailVO getDetail(Long memberId);

    List<MemberSocialBindingVO> listSocialBindings(Long memberId);

    Page<MemberLoginAuditVO> pageLoginAudits(Long memberId, AuthLoginLogQueryDTO queryDTO);

    Page<MemberManageRecordVO> pageManageRecords(Long memberId, PageQuery pageQuery);

    void disable(AdminMemberStatusUpdateDTO dto);

    void enable(AdminMemberStatusUpdateDTO dto);

    void freeze(AdminMemberStatusUpdateDTO dto);

    void unfreeze(AdminMemberStatusUpdateDTO dto);

    void forceLogout(AdminMemberForceLogoutDTO dto);
}