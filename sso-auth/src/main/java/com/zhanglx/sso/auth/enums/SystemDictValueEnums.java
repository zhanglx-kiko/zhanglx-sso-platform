package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public final class SystemDictValueEnums {

    /**
     * 私有构造方法，禁止外部实例化。
     */
    private SystemDictValueEnums() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * SystemDictValueEnums枚举。
     */
    @Getter
    @RequiredArgsConstructor
    public enum IdCardType implements BuiltInDictEnum {
        ID_CARD("1", "身份证"),
        PASSPORT("2", "护照"),
        OFFICER_CARD("3", "军官证"),
        DRIVER_LICENSE("4", "驾驶证"),
        HONG_KONG_MACAO_PASS("5", "港澳通行证");

        /**
         * 验证码。
         */
        @JsonValue
        private final String code;
        /**
         * 说明。
         */
        private final String description;

        public static IdCardType fromCode(String code) {
            return IBaseEnum.fromCode(code, IdCardType.class);
        }

        @Override
        public SystemDictTypeEnum getDictType() {
            return SystemDictTypeEnum.ID_CARD_TYPE;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum Education implements BuiltInDictEnum {
        PRIMARY("1", "小学"),
        JUNIOR_HIGH("2", "初中"),
        HIGH_SCHOOL("3", "高中"),
        JUNIOR_COLLEGE("4", "大专"),
        BACHELOR("5", "本科"),
        MASTER("6", "硕士研究生"),
        DOCTOR("7", "博士研究生");

        /**
         * 验证码。
         */
        @JsonValue
        private final String code;
        /**
         * 说明。
         */
        private final String description;

        public static Education fromCode(String code) {
            return IBaseEnum.fromCode(code, Education.class);
        }

        @Override
        public SystemDictTypeEnum getDictType() {
            return SystemDictTypeEnum.EDUCATION;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum MaritalStatus implements BuiltInDictEnum {
        SINGLE("1", "未婚"),
        MARRIED("2", "已婚"),
        DIVORCED("3", "离异"),
        WIDOWED("4", "丧偶");

        /**
         * 验证码。
         */
        @JsonValue
        private final String code;
        /**
         * 说明。
         */
        private final String description;

        public static MaritalStatus fromCode(String code) {
            return IBaseEnum.fromCode(code, MaritalStatus.class);
        }

        @Override
        public SystemDictTypeEnum getDictType() {
            return SystemDictTypeEnum.MARITAL_STATUS;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum PoliticalStatus implements BuiltInDictEnum {
        CPC_MEMBER("1", "中共党员"),
        CPC_PROBATIONARY_MEMBER("2", "中共预备党员"),
        LEAGUE_MEMBER("3", "共青团员"),
        MASSES("4", "群众"),
        REVOLUTIONARY_PARTY_MEMBER("5", "民革党员"),
        DEMOCRATIC_LEAGUE_MEMBER("6", "民盟盟员");

        /**
         * 验证码。
         */
        @JsonValue
        private final String code;
        /**
         * 说明。
         */
        private final String description;

        public static PoliticalStatus fromCode(String code) {
            return IBaseEnum.fromCode(code, PoliticalStatus.class);
        }

        @Override
        public SystemDictTypeEnum getDictType() {
            return SystemDictTypeEnum.POLITICAL_STATUS;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum LeaveType implements BuiltInDictEnum {
        PERSONAL("1", "事假"),
        SICK("2", "病假"),
        ANNUAL("3", "年假"),
        MARRIAGE("4", "婚假"),
        MATERNITY("5", "产假"),
        PATERNITY("6", "陪产假");

        /**
         * 验证码。
         */
        @JsonValue
        private final String code;
        /**
         * 说明。
         */
        private final String description;

        public static LeaveType fromCode(String code) {
            return IBaseEnum.fromCode(code, LeaveType.class);
        }

        @Override
        public SystemDictTypeEnum getDictType() {
            return SystemDictTypeEnum.LEAVE_TYPE;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum ApprovalStatus implements BuiltInDictEnum {
        PENDING_SUBMIT("0", "待提交"),
        PENDING_APPROVAL("1", "待审批"),
        APPROVED("2", "已通过"),
        REJECTED("3", "已拒绝"),
        CANCELED("4", "已撤回");

        /**
         * 验证码。
         */
        @JsonValue
        private final String code;
        /**
         * 说明。
         */
        private final String description;

        public static ApprovalStatus fromCode(String code) {
            return IBaseEnum.fromCode(code, ApprovalStatus.class);
        }

        @Override
        public SystemDictTypeEnum getDictType() {
            return SystemDictTypeEnum.APPROVAL_STATUS;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum PaymentMethod implements BuiltInDictEnum {
        CASH("1", "现金"),
        ALIPAY("2", "支付宝"),
        WECHAT_PAY("3", "微信支付"),
        BANK_CARD("4", "银行卡"),
        CREDIT_CARD("5", "信用卡");

        /**
         * 验证码。
         */
        @JsonValue
        private final String code;
        /**
         * 说明。
         */
        private final String description;

        public static PaymentMethod fromCode(String code) {
            return IBaseEnum.fromCode(code, PaymentMethod.class);
        }

        @Override
        public SystemDictTypeEnum getDictType() {
            return SystemDictTypeEnum.PAYMENT_METHOD;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum LogisticsStatus implements BuiltInDictEnum {
        PENDING_SHIPMENT("1", "待发货"),
        SHIPPED("2", "已发货"),
        IN_TRANSIT("3", "运输中"),
        SIGNED("4", "已签收"),
        REJECTED("5", "已拒收");

        /**
         * 验证码。
         */
        @JsonValue
        private final String code;
        /**
         * 说明。
         */
        private final String description;

        public static LogisticsStatus fromCode(String code) {
            return IBaseEnum.fromCode(code, LogisticsStatus.class);
        }

        @Override
        public SystemDictTypeEnum getDictType() {
            return SystemDictTypeEnum.LOGISTICS_STATUS;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum CustomerLevel implements BuiltInDictEnum {
        NORMAL("1", "普通客户"),
        VIP("2", "VIP客户"),
        SVIP("3", "SVIP客户"),
        DIAMOND("4", "钻石客户");

        /**
         * 验证码。
         */
        @JsonValue
        private final String code;
        /**
         * 说明。
         */
        private final String description;

        public static CustomerLevel fromCode(String code) {
            return IBaseEnum.fromCode(code, CustomerLevel.class);
        }

        @Override
        public SystemDictTypeEnum getDictType() {
            return SystemDictTypeEnum.CUSTOMER_LEVEL;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum ProductStatus implements BuiltInDictEnum {
        DRAFT("0", "草稿"),
        PENDING_REVIEW("1", "待审核"),
        ON_SHELF("2", "已上架"),
        OFF_SHELF("3", "已下架"),
        STOPPED("4", "已停售");

        /**
         * 验证码。
         */
        @JsonValue
        private final String code;
        /**
         * 说明。
         */
        private final String description;

        public static ProductStatus fromCode(String code) {
            return IBaseEnum.fromCode(code, ProductStatus.class);
        }

        @Override
        public SystemDictTypeEnum getDictType() {
            return SystemDictTypeEnum.PRODUCT_STATUS;
        }
    }

}