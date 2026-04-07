package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IStringBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemDictTypeEnum implements IStringBaseEnum<String> {

    ID_CARD_TYPE("sys_id_card_type", "证件类型"),
    EDUCATION("sys_education", "学历"),
    MARITAL_STATUS("sys_marital_status", "婚姻状况"),
    POLITICAL_STATUS("sys_political_status", "政治面貌"),
    LEAVE_TYPE("sys_leave_type", "请假类型"),
    APPROVAL_STATUS("sys_approval_status", "审批状态"),
    PAYMENT_METHOD("sys_payment_method", "支付方式"),
    LOGISTICS_STATUS("sys_logistics_status", "物流状态"),
    CUSTOMER_LEVEL("sys_customer_level", "客户等级"),
    PRODUCT_STATUS("sys_product_status", "产品状态");

    @JsonValue
    private final String code;

    private final String description;

    public static SystemDictTypeEnum fromCode(String code) {
        return IBaseEnum.fromCode(code, SystemDictTypeEnum.class);
    }

}
