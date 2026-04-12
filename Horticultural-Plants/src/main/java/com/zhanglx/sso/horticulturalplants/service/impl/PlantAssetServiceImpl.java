package com.zhanglx.sso.horticulturalplants.service.impl;

import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.horticulturalplants.config.PlantStorageProperties;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantUploadImageVO;
import com.zhanglx.sso.horticulturalplants.exception.PlantErrorCode;
import com.zhanglx.sso.horticulturalplants.service.PlantAssetService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PlantAssetServiceImpl implements PlantAssetService {

    private static final int MAX_FILE_COUNT = 9;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp", ".svg");

    private final PlantStorageProperties plantStorageProperties;

    public PlantAssetServiceImpl(PlantStorageProperties plantStorageProperties) {
        this.plantStorageProperties = plantStorageProperties;
    }

    @Override
    public List<PlantUploadImageVO> uploadImages(Long memberId, List<MultipartFile> files) {
        AssertUtils.notNull(memberId, PlantErrorCode.PLANT_MEMBER_NOT_FOUND);
        AssertUtils.notEmpty(files, PlantErrorCode.PLANT_UPLOAD_FILE_REQUIRED);
        AssertUtils.isTrue(files.size() <= MAX_FILE_COUNT, PlantErrorCode.PLANT_UPLOAD_FILE_COUNT_INVALID);

        Path rootDirectory = resolveRootDirectory();
        createDirectories(rootDirectory);

        String dateSegment = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path dayDirectory = rootDirectory.resolve(dateSegment);
        createDirectories(dayDirectory);

        List<PlantUploadImageVO> results = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            validateFile(file);
            String extension = resolveExtension(file.getOriginalFilename());
            String storedFileName = UUID.randomUUID().toString().replace("-", "") + extension;
            Path targetPath = dayDirectory.resolve(storedFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw BusinessException.of(PlantErrorCode.PLANT_STORAGE_WRITE_FAILED, e);
            }
            results.add(PlantUploadImageVO.builder()
                    .fileName(file.getOriginalFilename())
                    .url(buildPublicUrl(dateSegment + "/" + storedFileName))
                    .size(file.getSize())
                    .build());
        }
        return results;
    }

    private void validateFile(MultipartFile file) {
        AssertUtils.notNull(file, PlantErrorCode.PLANT_UPLOAD_FILE_REQUIRED);
        AssertUtils.isTrue(!file.isEmpty(), PlantErrorCode.PLANT_UPLOAD_FILE_REQUIRED);
        AssertUtils.isTrue(file.getSize() <= MAX_FILE_SIZE, PlantErrorCode.PLANT_UPLOAD_FILE_TOO_LARGE);
        AssertUtils.isTrue(ALLOWED_EXTENSIONS.contains(resolveExtension(file.getOriginalFilename())),
                PlantErrorCode.PLANT_UPLOAD_FILE_TYPE_INVALID);
    }

    private Path resolveRootDirectory() {
        String configuredBasePath = StringUtils.hasText(plantStorageProperties.getBasePath())
                ? plantStorageProperties.getBasePath().trim()
                : "./storage/member-plant-assets/";
        return Paths.get(configuredBasePath).toAbsolutePath().normalize();
    }

    private void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (Exception e) {
            throw BusinessException.of(PlantErrorCode.PLANT_STORAGE_CREATE_FAILED, e);
        }
    }

    private String buildPublicUrl(String relativePath) {
        String publicUrlPrefix = StringUtils.hasText(plantStorageProperties.getPublicUrlPrefix())
                ? plantStorageProperties.getPublicUrlPrefix().trim()
                : "/apis/v1/horticultural-plants/public/assets/";
        if (!publicUrlPrefix.endsWith("/")) {
            publicUrlPrefix = publicUrlPrefix + "/";
        }
        return publicUrlPrefix + relativePath;
    }

    private String resolveExtension(String originalFilename) {
        String filename = StringUtils.hasText(originalFilename) ? originalFilename.trim() : "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}
