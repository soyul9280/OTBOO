package com.codeit.weatherwear.global.storage.s3;

import com.codeit.weatherwear.global.exception.s3.S3DeleteException;
import com.codeit.weatherwear.global.exception.s3.S3PresignedException;
import com.codeit.weatherwear.global.exception.s3.S3UploadException;
import com.codeit.weatherwear.global.storage.ThumbnailImageStorage;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ThumbnailImageStorage implements ThumbnailImageStorage {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private static final String CONTENT_TYPE_IMAGE_PREFIX = "image/";

  @Value("${weatherwear.storage.s3.bucket}")
  private String bucket;

  @Value("${weatherwear.storage.s3.presigned-url-expiration}")
  private long presignedUrlExpirationSeconds;

  // S3에 이미지를 저장하고 해당 파일의 key를 반환
  @Override
  public String upload(MultipartFile file) {
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith(CONTENT_TYPE_IMAGE_PREFIX)) {
      String ext = extractExtension(file.getOriginalFilename());

      contentType = switch (ext) {
        case "jpg", "jpeg" -> "image/jpeg";
        case "png" -> "image/png";
        case "gif" -> "image/gif";
        case "webp" -> "image/webp";
        default -> {
          log.warn("[S3 Upload Fail] Unknown Media Type: {}", ext);
          throw new S3UploadException();
        }
      };
    }

    String key = CONTENT_TYPE_IMAGE_PREFIX + UUID.randomUUID();

    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(file.getContentType())
            .build();

    try {
      s3Client.putObject(
          request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
      log.info("[S3 Upload Success] Key: {}", key);
    } catch (IOException | SdkClientException | S3Exception e) {
      log.error("[S3 Upload Fail] FileName: {}, Error: {}", file.getOriginalFilename(),
          e.toString());
      throw new S3UploadException();
    }
    return key;
  }

  //주어진 URL에 해당하는 S3객체 삭제
  @Override
  public void delete(String url) {
    String key = extractKeyFromUrl(url);
    log.info("[S3 Delete Request] Key: {}", key);
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder().
          bucket(bucket)
          .key(key)
          .build());
      log.info("[S3 Delete Success] Key: {}", key);
    } catch (SdkClientException | S3Exception e) {
      log.error("[S3 Delete Fail] Key: {}, Error: {}", key, e.toString());
      throw new S3DeleteException();
    }
  }

  // 주어진 key에 대해 Presigned URL을 생성하여 반환
  // Presigned URL은 일시적으로 접근 가능한 S3 다운로드 링크
  @Override
  public String get(String key) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();
    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
            .getObjectRequest(getObjectRequest)
            .build();
    try {
      PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
      log.info("[Presigned URL Create Complete] Key: {}, URL: {}", key, presignedRequest.url());
      return presignedRequest.url().toString();
    } catch (SdkClientException | S3Exception e) {
      log.error("[Presigned URL Create Fail] Key: {}, Error: {}", key, e.toString());
      throw new S3PresignedException();
    }
  }

  // https://bucket.s3.region.amazonaws.com/key 형식에서 key만 추출
  private String extractKeyFromUrl(String url) {
    URI uri = URI.create(url);
    String path = uri.getPath();
    if (path == null || path.length() <= 1) {
      throw new IllegalArgumentException("Not Supported S3 URL: " + url);
    }
    String key = path.substring(1);
    log.debug("[S3 Key Extract] URL: {}, Extracted Key: {}", url, key);
    return key;
  }

  private String extractExtension(String filename) {
    if (filename == null) {
      return "";
    }

    // 쿼리 스트링 제거 (e.g. jpeg?width=400 → jpeg)
    String cleanName = filename.split("\\?")[0];

    int lastDot = cleanName.lastIndexOf('.');
    if (lastDot == -1) {
      return "";
    }

    return cleanName.substring(lastDot + 1).toLowerCase();
  }


}
