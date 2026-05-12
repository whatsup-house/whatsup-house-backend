package com.whatsuphouse.backend.global.storage.service;

import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SupabaseStorageService implements StorageService{

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final RestClient restClient = RestClient.create();

    @Override
    public String upload(MultipartFile file, String folder) {
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
        }

        String fileName = UUID.randomUUID() + "." + extension;
        String tempPath = "temp/" + folder + "/" + fileName;

        try {
            restClient.put()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + tempPath)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (IOException e) {
            log.error("[Storage] upload IOException: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        } catch (Exception e) {
            log.error("[Storage] upload Exception: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return tempPath;
    }

    @Override
    public String move(String tempPath, String targetFolder) {
        String fileName = tempPath.substring(tempPath.lastIndexOf('/') + 1);
        String destinationKey = targetFolder + "/" + fileName;

        try {
            restClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/move")
                    .header("Authorization", "Bearer " + supabaseKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "bucketId", bucket,
                            "sourceKey", tempPath,
                            "destinationKey", destinationKey
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("[Storage] move Exception: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return getPublicUrl(destinationKey);
    }

    @Override
    public String getPublicUrl(String path) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    @Override
    public void delete(String path) {
        try {
            restClient.delete()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + path)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
