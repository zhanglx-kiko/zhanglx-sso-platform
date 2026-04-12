package com.zhanglx.sso.horticulturalplants.service;

import com.zhanglx.sso.horticulturalplants.domain.vo.PlantUploadImageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PlantAssetService {

    List<PlantUploadImageVO> uploadImages(Long memberId, List<MultipartFile> files);
}
