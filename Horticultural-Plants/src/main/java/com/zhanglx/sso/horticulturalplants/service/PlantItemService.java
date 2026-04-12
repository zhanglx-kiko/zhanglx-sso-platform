package com.zhanglx.sso.horticulturalplants.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.horticulturalplants.domain.dto.PlantItemPageQueryDTO;
import com.zhanglx.sso.horticulturalplants.domain.dto.PlantItemSaveDTO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantCategoryVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantItemCardVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantItemDetailVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantMineSummaryVO;
import com.zhanglx.sso.horticulturalplants.enums.PlantItemPublishStatusEnum;

import java.util.List;

public interface PlantItemService {

    List<PlantCategoryVO> listCategories();

    Page<PlantItemCardVO> pagePublishedItems(PlantItemPageQueryDTO queryDTO);

    PlantItemDetailVO getItemDetail(Long itemId, Long currentMemberId);

    Page<PlantItemCardVO> pageMyItems(Long memberId, PlantItemPageQueryDTO queryDTO);

    PlantMineSummaryVO getMySummary(Long memberId);

    PlantItemDetailVO createItem(Long memberId, PlantItemSaveDTO saveDTO);

    PlantItemDetailVO updateItem(Long memberId, Long itemId, PlantItemSaveDTO saveDTO);

    void updatePublishStatus(Long memberId, Long itemId, PlantItemPublishStatusEnum publishStatus);

    void deleteItem(Long memberId, Long itemId);
}
