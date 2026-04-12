package com.zhanglx.sso.horticulturalplants.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.horticulturalplants.domain.dto.PlantItemPageQueryDTO;
import com.zhanglx.sso.horticulturalplants.domain.dto.PlantItemSaveDTO;
import com.zhanglx.sso.horticulturalplants.domain.po.PlantCategoryPO;
import com.zhanglx.sso.horticulturalplants.domain.po.PlantItemImagePO;
import com.zhanglx.sso.horticulturalplants.domain.po.PlantItemPO;
import com.zhanglx.sso.horticulturalplants.domain.po.PlantMemberUserPO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantCategoryVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantItemCardVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantItemDetailVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantItemImageVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantMineSummaryVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantPublisherVO;
import com.zhanglx.sso.horticulturalplants.enums.EnableStatusEnum;
import com.zhanglx.sso.horticulturalplants.enums.PlantItemPublishStatusEnum;
import com.zhanglx.sso.horticulturalplants.enums.YesNoEnum;
import com.zhanglx.sso.horticulturalplants.exception.PlantErrorCode;
import com.zhanglx.sso.horticulturalplants.mapper.PlantCategoryMapper;
import com.zhanglx.sso.horticulturalplants.mapper.PlantItemImageMapper;
import com.zhanglx.sso.horticulturalplants.mapper.PlantItemMapper;
import com.zhanglx.sso.horticulturalplants.mapper.PlantMemberUserMapper;
import com.zhanglx.sso.horticulturalplants.service.PlantItemService;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlantItemServiceImpl implements PlantItemService {

    private static final Integer MEMBER_STATUS_NORMAL = 1;

    private final PlantCategoryMapper plantCategoryMapper;
    private final PlantItemMapper plantItemMapper;
    private final PlantItemImageMapper plantItemImageMapper;
    private final PlantMemberUserMapper plantMemberUserMapper;

    @Override
    public List<PlantCategoryVO> listCategories() {
        return plantCategoryMapper.selectList(new LambdaQueryWrapperX<PlantCategoryPO>()
                        .eq(PlantCategoryPO::getStatus, EnableStatusEnum.ENABLED)
                        .orderByAsc(PlantCategoryPO::getSortNum)
                        .orderByDesc(PlantCategoryPO::getCreateTime))
                .stream()
                .map(this::toCategoryVO)
                .toList();
    }

    @Override
    public Page<PlantItemCardVO> pagePublishedItems(PlantItemPageQueryDTO queryDTO) {
        PlantItemPageQueryDTO actualQuery = queryDTO == null ? PlantItemPageQueryDTO.builder().build() : queryDTO;
        Page<PlantItemPO> page = Page.of(actualQuery.getPageNum(), actualQuery.getPageSize());
        LambdaQueryWrapperX<PlantItemPO> wrapper = buildBaseQueryWrapper(actualQuery)
                .eq(PlantItemPO::getPublishStatus, PlantItemPublishStatusEnum.PUBLISHED)
                .orderByDesc(PlantItemPO::getUpdateTime)
                .orderByDesc(PlantItemPO::getCreateTime);
        plantItemMapper.selectPage(page, wrapper);
        return buildPage(page, buildCardVOList(page.getRecords()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlantItemDetailVO getItemDetail(Long itemId, Long currentMemberId) {
        PlantItemPO itemPO = getItemOrThrow(itemId);
        boolean owner = isOwner(itemPO, currentMemberId);
        if (!owner && !PlantItemPublishStatusEnum.PUBLISHED.matches(itemPO.getPublishStatus())) {
            throw new BusinessException(PlantErrorCode.PLANT_ITEM_NOT_FOUND);
        }

        plantItemMapper.update(null, new LambdaUpdateWrapper<PlantItemPO>()
                .eq(PlantItemPO::getId, itemId)
                .setSql("view_count = view_count + 1"));
        itemPO.setViewCount(Optional.ofNullable(itemPO.getViewCount()).orElse(0L) + 1);
        return toDetailVO(itemPO, queryImages(itemId), queryPublisher(itemPO.getPublisherUserId()));
    }

    @Override
    public Page<PlantItemCardVO> pageMyItems(Long memberId, PlantItemPageQueryDTO queryDTO) {
        ensureCurrentMemberAvailable(memberId);
        PlantItemPageQueryDTO actualQuery = queryDTO == null ? PlantItemPageQueryDTO.builder().build() : queryDTO;
        Page<PlantItemPO> page = Page.of(actualQuery.getPageNum(), actualQuery.getPageSize());
        LambdaQueryWrapperX<PlantItemPO> wrapper = buildBaseQueryWrapper(actualQuery)
                .eq(PlantItemPO::getPublisherUserId, memberId)
                .eqIfPresent(PlantItemPO::getPublishStatus, actualQuery.getPublishStatus())
                .orderByDesc(PlantItemPO::getUpdateTime)
                .orderByDesc(PlantItemPO::getCreateTime);
        plantItemMapper.selectPage(page, wrapper);
        return buildPage(page, buildCardVOList(page.getRecords()));
    }

    @Override
    public PlantMineSummaryVO getMySummary(Long memberId) {
        ensureCurrentMemberAvailable(memberId);
        return PlantMineSummaryVO.builder()
                .totalCount(countMyItems(memberId, null))
                .publishedCount(countMyItems(memberId, PlantItemPublishStatusEnum.PUBLISHED))
                .draftCount(countMyItems(memberId, PlantItemPublishStatusEnum.DRAFT))
                .offShelfCount(countMyItems(memberId, PlantItemPublishStatusEnum.OFF_SHELF))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlantItemDetailVO createItem(Long memberId, PlantItemSaveDTO saveDTO) {
        ensureCurrentMemberAvailable(memberId);
        PlantCategoryPO categoryPO = getCategoryOrThrow(saveDTO.getCategoryId());
        String coverImageUrl = resolveCoverImage(saveDTO.getCoverImageUrl(), saveDTO.getImageUrls());
        PlantItemPO itemPO = PlantItemPO.builder()
                .publisherUserId(memberId)
                .categoryId(categoryPO.getId())
                .categoryName(categoryPO.getCategoryName())
                .title(trim(saveDTO.getTitle()))
                .coverImageUrl(coverImageUrl)
                .suggestedRetailPrice(saveDTO.getSuggestedRetailPrice())
                .unit(trim(saveDTO.getUnit()))
                .shortDescription(trim(saveDTO.getShortDescription()))
                .detailDescription(trim(saveDTO.getDetailDescription()))
                .province(trim(saveDTO.getProvince()))
                .city(trim(saveDTO.getCity()))
                .area(trim(saveDTO.getArea()))
                .publishStatus(resolvePublishStatus(saveDTO.getPublishStatus()))
                .viewCount(0L)
                .build();
        plantItemMapper.insert(itemPO);
        syncImages(itemPO.getId(), saveDTO.getImageUrls(), coverImageUrl);
        return toDetailVO(itemPO, queryImages(itemPO.getId()), queryPublisher(memberId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlantItemDetailVO updateItem(Long memberId, Long itemId, PlantItemSaveDTO saveDTO) {
        ensureCurrentMemberAvailable(memberId);
        PlantItemPO itemPO = getItemOrThrow(itemId);
        assertOwner(itemPO, memberId);
        PlantCategoryPO categoryPO = getCategoryOrThrow(saveDTO.getCategoryId());
        String coverImageUrl = resolveCoverImage(saveDTO.getCoverImageUrl(), saveDTO.getImageUrls());
        plantItemMapper.update(null, new LambdaUpdateWrapper<PlantItemPO>()
                .eq(PlantItemPO::getId, itemId)
                .set(PlantItemPO::getCategoryId, categoryPO.getId())
                .set(PlantItemPO::getCategoryName, categoryPO.getCategoryName())
                .set(PlantItemPO::getTitle, trim(saveDTO.getTitle()))
                .set(PlantItemPO::getCoverImageUrl, coverImageUrl)
                .set(PlantItemPO::getSuggestedRetailPrice, saveDTO.getSuggestedRetailPrice())
                .set(PlantItemPO::getUnit, trim(saveDTO.getUnit()))
                .set(PlantItemPO::getShortDescription, trim(saveDTO.getShortDescription()))
                .set(PlantItemPO::getDetailDescription, trim(saveDTO.getDetailDescription()))
                .set(PlantItemPO::getProvince, trim(saveDTO.getProvince()))
                .set(PlantItemPO::getCity, trim(saveDTO.getCity()))
                .set(PlantItemPO::getArea, trim(saveDTO.getArea()))
                .set(PlantItemPO::getPublishStatus, resolvePublishStatus(saveDTO.getPublishStatus())));
        syncImages(itemId, saveDTO.getImageUrls(), coverImageUrl);
        itemPO.setCategoryId(categoryPO.getId());
        itemPO.setCategoryName(categoryPO.getCategoryName());
        itemPO.setTitle(trim(saveDTO.getTitle()));
        itemPO.setCoverImageUrl(coverImageUrl);
        itemPO.setSuggestedRetailPrice(saveDTO.getSuggestedRetailPrice());
        itemPO.setUnit(trim(saveDTO.getUnit()));
        itemPO.setShortDescription(trim(saveDTO.getShortDescription()));
        itemPO.setDetailDescription(trim(saveDTO.getDetailDescription()));
        itemPO.setProvince(trim(saveDTO.getProvince()));
        itemPO.setCity(trim(saveDTO.getCity()));
        itemPO.setArea(trim(saveDTO.getArea()));
        itemPO.setPublishStatus(resolvePublishStatus(saveDTO.getPublishStatus()));
        return toDetailVO(itemPO, queryImages(itemId), queryPublisher(memberId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePublishStatus(Long memberId, Long itemId, PlantItemPublishStatusEnum publishStatus) {
        ensureCurrentMemberAvailable(memberId);
        PlantItemPO itemPO = getItemOrThrow(itemId);
        assertOwner(itemPO, memberId);
        plantItemMapper.update(null, new LambdaUpdateWrapper<PlantItemPO>()
                .eq(PlantItemPO::getId, itemId)
                .set(PlantItemPO::getPublishStatus, resolvePublishStatus(publishStatus)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long memberId, Long itemId) {
        ensureCurrentMemberAvailable(memberId);
        PlantItemPO itemPO = getItemOrThrow(itemId);
        assertOwner(itemPO, memberId);
        plantItemMapper.deleteByIdWithFill(itemId);
        softDeleteImages(itemId);
    }

    private LambdaQueryWrapperX<PlantItemPO> buildBaseQueryWrapper(PlantItemPageQueryDTO queryDTO) {
        LambdaQueryWrapperX<PlantItemPO> wrapper = new LambdaQueryWrapperX<PlantItemPO>()
                .eqIfPresent(PlantItemPO::getCategoryId, queryDTO.getCategoryId())
                .eqIfPresent(PlantItemPO::getProvince, trim(queryDTO.getProvince()))
                .eqIfPresent(PlantItemPO::getCity, trim(queryDTO.getCity()));
        if (StringUtils.hasText(queryDTO.getSearchKey())) {
            String searchKey = queryDTO.getSearchKey().trim();
            wrapper.and(w -> w.like(PlantItemPO::getTitle, searchKey)
                    .or()
                    .like(PlantItemPO::getShortDescription, searchKey)
                    .or()
                    .like(PlantItemPO::getCategoryName, searchKey)
                    .or()
                    .like(PlantItemPO::getCity, searchKey));
        }
        return wrapper;
    }

    private PlantCategoryPO getCategoryOrThrow(Long categoryId) {
        AssertUtils.notNull(categoryId, PlantErrorCode.PLANT_CATEGORY_NOT_FOUND);
        PlantCategoryPO categoryPO = plantCategoryMapper.selectById(categoryId);
        AssertUtils.notNull(categoryPO, PlantErrorCode.PLANT_CATEGORY_NOT_FOUND);
        AssertUtils.isTrue(EnableStatusEnum.ENABLED.matches(categoryPO.getStatus()), PlantErrorCode.PLANT_CATEGORY_DISABLED);
        return categoryPO;
    }

    private PlantItemPO getItemOrThrow(Long itemId) {
        AssertUtils.notNull(itemId, PlantErrorCode.PLANT_ITEM_NOT_FOUND);
        PlantItemPO itemPO = plantItemMapper.selectById(itemId);
        AssertUtils.notNull(itemPO, PlantErrorCode.PLANT_ITEM_NOT_FOUND);
        return itemPO;
    }

    private void ensureCurrentMemberAvailable(Long memberId) {
        AssertUtils.notNull(memberId, PlantErrorCode.PLANT_MEMBER_NOT_FOUND);
        PlantMemberUserPO memberUserPO = plantMemberUserMapper.selectById(memberId);
        AssertUtils.notNull(memberUserPO, PlantErrorCode.PLANT_MEMBER_NOT_FOUND);
        AssertUtils.isTrue(Objects.equals(memberUserPO.getStatus(), MEMBER_STATUS_NORMAL), PlantErrorCode.PLANT_MEMBER_STATUS_INVALID);
    }

    private void assertOwner(PlantItemPO itemPO, Long memberId) {
        AssertUtils.isTrue(isOwner(itemPO, memberId), PlantErrorCode.PLANT_ITEM_NO_PERMISSION);
    }

    private boolean isOwner(PlantItemPO itemPO, Long memberId) {
        return itemPO != null && memberId != null && Objects.equals(itemPO.getPublisherUserId(), memberId);
    }

    private PlantItemPublishStatusEnum resolvePublishStatus(PlantItemPublishStatusEnum publishStatus) {
        return publishStatus == null ? PlantItemPublishStatusEnum.PUBLISHED : publishStatus;
    }

    private String resolveCoverImage(String requestedCoverImageUrl, List<String> imageUrls) {
        AssertUtils.notEmpty(imageUrls, PlantErrorCode.PLANT_ITEM_IMAGE_REQUIRED);
        List<String> normalizedImages = imageUrls.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        AssertUtils.notEmpty(normalizedImages, PlantErrorCode.PLANT_ITEM_IMAGE_REQUIRED);
        if (StringUtils.hasText(requestedCoverImageUrl)) {
            String coverImageUrl = requestedCoverImageUrl.trim();
            AssertUtils.isTrue(normalizedImages.contains(coverImageUrl), PlantErrorCode.PLANT_ITEM_COVER_INVALID);
            return coverImageUrl;
        }
        return normalizedImages.getFirst();
    }

    private void syncImages(Long itemId, List<String> imageUrls, String coverImageUrl) {
        softDeleteImages(itemId);
        int sortNum = 1;
        for (String imageUrl : imageUrls.stream().filter(StringUtils::hasText).map(String::trim).distinct().toList()) {
            plantItemImageMapper.insert(PlantItemImagePO.builder()
                    .plantItemId(itemId)
                    .imageUrl(imageUrl)
                    .sortNum(sortNum++)
                    .coverFlag(Objects.equals(imageUrl, coverImageUrl) ? YesNoEnum.YES : YesNoEnum.NO)
                    .build());
        }
    }

    private void softDeleteImages(Long itemId) {
        plantItemImageMapper.update(null, new UpdateWrapper<PlantItemImagePO>()
                .setSql("del_flag = id")
                .eq("plant_item_id", itemId)
                .eq("del_flag", 0));
    }

    private List<PlantItemImagePO> queryImages(Long itemId) {
        return plantItemImageMapper.selectList(new LambdaQueryWrapperX<PlantItemImagePO>()
                .eq(PlantItemImagePO::getPlantItemId, itemId)
                .orderByAsc(PlantItemImagePO::getSortNum)
                .orderByDesc(PlantItemImagePO::getCreateTime));
    }

    private PlantMemberUserPO queryPublisher(Long publisherUserId) {
        return publisherUserId == null ? null : plantMemberUserMapper.selectById(publisherUserId);
    }

    private Page<PlantItemCardVO> buildPage(Page<PlantItemPO> source, List<PlantItemCardVO> records) {
        Page<PlantItemCardVO> page = new Page<>();
        page.setCurrent(source.getCurrent());
        page.setSize(source.getSize());
        page.setTotal(source.getTotal());
        page.setRecords(records);
        return page;
    }

    private List<PlantItemCardVO> buildCardVOList(List<PlantItemPO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Map<Long, PlantMemberUserPO> publisherMap = queryPublisherMap(records.stream().map(PlantItemPO::getPublisherUserId).toList());
        return records.stream()
                .map(itemPO -> toCardVO(itemPO, publisherMap.get(itemPO.getPublisherUserId())))
                .toList();
    }

    private Map<Long, PlantMemberUserPO> queryPublisherMap(Collection<Long> memberIds) {
        List<Long> normalizedIds = memberIds == null ? List.of() : memberIds.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, PlantMemberUserPO> result = new HashMap<>();
        plantMemberUserMapper.selectList(new LambdaQueryWrapper<PlantMemberUserPO>()
                        .in(PlantMemberUserPO::getId, normalizedIds))
                .forEach(member -> result.put(member.getId(), member));
        return result;
    }

    private long countMyItems(Long memberId, PlantItemPublishStatusEnum status) {
        return Optional.ofNullable(plantItemMapper.selectCount(new LambdaQueryWrapperX<PlantItemPO>()
                        .eq(PlantItemPO::getPublisherUserId, memberId)
                        .eqIfPresent(PlantItemPO::getPublishStatus, status)))
                .orElse(0L);
    }

    private PlantCategoryVO toCategoryVO(PlantCategoryPO categoryPO) {
        return PlantCategoryVO.builder()
                .id(categoryPO.getId())
                .createBy(categoryPO.getCreateBy())
                .createTime(categoryPO.getCreateTime())
                .updateBy(categoryPO.getUpdateBy())
                .updateTime(categoryPO.getUpdateTime())
                .categoryName(categoryPO.getCategoryName())
                .categoryCode(categoryPO.getCategoryCode())
                .description(categoryPO.getDescription())
                .build();
    }

    private PlantItemCardVO toCardVO(PlantItemPO itemPO, PlantMemberUserPO publisher) {
        return PlantItemCardVO.builder()
                .id(itemPO.getId())
                .createBy(itemPO.getCreateBy())
                .createTime(itemPO.getCreateTime())
                .updateBy(itemPO.getUpdateBy())
                .updateTime(itemPO.getUpdateTime())
                .categoryId(itemPO.getCategoryId())
                .categoryName(itemPO.getCategoryName())
                .title(itemPO.getTitle())
                .coverImageUrl(itemPO.getCoverImageUrl())
                .suggestedRetailPrice(itemPO.getSuggestedRetailPrice())
                .unit(itemPO.getUnit())
                .shortDescription(itemPO.getShortDescription())
                .province(itemPO.getProvince())
                .city(itemPO.getCity())
                .viewCount(itemPO.getViewCount())
                .publishStatus(itemPO.getPublishStatus())
                .publisherName(resolvePublisherName(publisher))
                .publisherAvatar(publisher == null ? null : publisher.getAvatar())
                .build();
    }

    private PlantItemDetailVO toDetailVO(PlantItemPO itemPO, List<PlantItemImagePO> images, PlantMemberUserPO publisher) {
        return PlantItemDetailVO.builder()
                .id(itemPO.getId())
                .createBy(itemPO.getCreateBy())
                .createTime(itemPO.getCreateTime())
                .updateBy(itemPO.getUpdateBy())
                .updateTime(itemPO.getUpdateTime())
                .categoryId(itemPO.getCategoryId())
                .categoryName(itemPO.getCategoryName())
                .title(itemPO.getTitle())
                .coverImageUrl(itemPO.getCoverImageUrl())
                .suggestedRetailPrice(itemPO.getSuggestedRetailPrice())
                .unit(itemPO.getUnit())
                .shortDescription(itemPO.getShortDescription())
                .detailDescription(itemPO.getDetailDescription())
                .province(itemPO.getProvince())
                .city(itemPO.getCity())
                .area(itemPO.getArea())
                .publishStatus(itemPO.getPublishStatus())
                .viewCount(itemPO.getViewCount())
                .imageList(images.stream().map(this::toImageVO).toList())
                .publisher(toPublisherVO(publisher))
                .build();
    }

    private PlantItemImageVO toImageVO(PlantItemImagePO imagePO) {
        return PlantItemImageVO.builder()
                .imageUrl(imagePO.getImageUrl())
                .cover(YesNoEnum.YES.matches(imagePO.getCoverFlag()))
                .sortNum(imagePO.getSortNum())
                .build();
    }

    private PlantPublisherVO toPublisherVO(PlantMemberUserPO publisher) {
        if (publisher == null) {
            return null;
        }
        return PlantPublisherVO.builder()
                .id(publisher.getId())
                .nickname(resolvePublisherName(publisher))
                .avatar(publisher.getAvatar())
                .build();
    }

    private String resolvePublisherName(PlantMemberUserPO publisher) {
        if (publisher == null) {
            return "匿名发布者";
        }
        if (StringUtils.hasText(publisher.getNickname())) {
            return publisher.getNickname();
        }
        if (StringUtils.hasText(publisher.getPhoneNumber())) {
            String phoneNumber = publisher.getPhoneNumber().trim();
            if (phoneNumber.length() >= 7) {
                return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
            }
            return phoneNumber;
        }
        return "园艺发布者";
    }

    private String trim(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }
}
