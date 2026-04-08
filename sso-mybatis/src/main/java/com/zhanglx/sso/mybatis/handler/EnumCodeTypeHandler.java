package com.zhanglx.sso.mybatis.handler;

import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.utils.enums.EnumUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/5 16:47
 * @ClassName: EnumCodeTypeHandler
 * @Description:
 */
public class EnumCodeTypeHandler<E extends Enum<?> & IBaseEnum> extends BaseTypeHandler<E> {
    private final Class<E> type;

    public EnumCodeTypeHandler() {
        this.type = null;
    }

    public EnumCodeTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.getCode());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // MySQL 驱动在 tinyint(1) 场景下可能把 0/1/2 等值按布尔语义返回，
        // 使用 getString 可以保留数据库中的原始字面值，避免枚举 code 被错误压扁成 true/false。
        String code = rs.getString(columnName);
        return code == null ? null : EnumUtils.codeOf(this.type, code);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code == null ? null : EnumUtils.codeOf(this.type, code);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code == null ? null : EnumUtils.codeOf(this.type, code);
    }

}
