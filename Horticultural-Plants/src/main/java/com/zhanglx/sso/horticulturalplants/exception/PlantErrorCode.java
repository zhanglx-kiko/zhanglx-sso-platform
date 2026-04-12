package com.zhanglx.sso.horticulturalplants.exception;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlantErrorCode implements ErrorCode {

    PLANT_CATEGORY_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "plant.category.not.found"),
    PLANT_CATEGORY_DISABLED(ResultCode.CONFLICT.getCode(), "plant.category.disabled"),
    PLANT_ITEM_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "plant.item.not.found"),
    PLANT_ITEM_NO_PERMISSION(ResultCode.FORBIDDEN.getCode(), "plant.item.no.permission"),
    PLANT_ITEM_IMAGE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "plant.item.image.required"),
    PLANT_ITEM_COVER_INVALID(ResultCode.BAD_REQUEST.getCode(), "plant.item.cover.invalid"),
    PLANT_MEMBER_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "plant.member.not.found"),
    PLANT_MEMBER_STATUS_INVALID(ResultCode.FORBIDDEN.getCode(), "plant.member.status.invalid"),
    PLANT_UPLOAD_FILE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "plant.upload.file.required"),
    PLANT_UPLOAD_FILE_COUNT_INVALID(ResultCode.BAD_REQUEST.getCode(), "plant.upload.file.count.invalid"),
    PLANT_UPLOAD_FILE_TOO_LARGE(ResultCode.BAD_REQUEST.getCode(), "plant.upload.file.too.large"),
    PLANT_UPLOAD_FILE_TYPE_INVALID(ResultCode.BAD_REQUEST.getCode(), "plant.upload.file.type.invalid"),
    PLANT_STORAGE_CREATE_FAILED(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "plant.storage.create.failed"),
    PLANT_STORAGE_WRITE_FAILED(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "plant.storage.write.failed");

    private final Integer code;

    private final String messageKey;
}
