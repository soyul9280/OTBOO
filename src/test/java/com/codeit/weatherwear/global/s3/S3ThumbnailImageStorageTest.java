package com.codeit.weatherwear.global.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import com.codeit.weatherwear.global.exception.s3.S3DeleteException;
import com.codeit.weatherwear.global.exception.s3.S3PresignedException;
import com.codeit.weatherwear.global.exception.s3.S3UploadException;
import com.codeit.weatherwear.global.storage.s3.S3ThumbnailImageStorage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
public class S3ThumbnailImageStorageTest {

  @Mock
  S3Client s3Client;

  @Mock
  S3Presigner s3Presigner;

  @Mock
  private MultipartFile multipartFile;

  @InjectMocks
  S3ThumbnailImageStorage storage;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(storage, "bucket", "my-bucket");
    ReflectionTestUtils.setField(storage, "presignedUrlExpirationSeconds", 600L);
  }

  @Test
  @DisplayName("upload 성공")
  void upload_success() throws Exception {
    // given
    given(multipartFile.getContentType()).willReturn("image/png");
    given(multipartFile.getInputStream()).willReturn(
        new ByteArrayInputStream("mock image".getBytes()));
    given(multipartFile.getSize()).willReturn(10L);

    // when
    String key = storage.upload(multipartFile);

    // then
    assertThat(key).startsWith("image/");
    then(s3Client).should().putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName("upload 실패 - 지원하지 않는 미디어타입")
  void upload_unsupportedMediaType_throwsException() {
    // given
    given(multipartFile.getContentType()).willReturn("application/pdf");

    // when, then
    assertThatThrownBy(() -> storage.upload(multipartFile))
        .isInstanceOf(S3UploadException.class);
  }

  @Test
  @DisplayName("presignedUrl 얻기 성공")
  void get_presignedUrl_success() throws Exception {
    // given
    String expectedUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/image/123";
    PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
    given(presigned.url()).willReturn(new URL(expectedUrl));
    given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).willReturn(presigned);

    // when
    String actualUrl = storage.get("image/123");

    // then
    assertThat(actualUrl).isEqualTo(expectedUrl);
  }

  @Test
  @DisplayName("get 실패 - S3Presigner 예외 발생")
  void get_presignedUrl_fail() {
    // given
    String key = "image/fail-key";
    willThrow(SdkClientException.builder().message("error").build())
        .given(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));

    // when, then
    assertThatThrownBy(() -> storage.get(key))
        .isInstanceOf(S3PresignedException.class);
  }


  @Test
  @DisplayName("delete 성공 - S3에서 객체 삭제")
  void delete_success() {
    // given
    String url = "https://my-bucket.s3.ap-northeast-2.amazonaws.com/image/abc123";

    // when
    storage.delete(url);

    // then
    then(s3Client).should().deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  @DisplayName("delete 실패 - S3Client 예외 발생")
  void delete_s3Exception_throwsException() {
    // given
    String url = "https://my-bucket.s3.ap-northeast-2.amazonaws.com/image/abc123";
    willThrow(S3Exception.builder().message("Error").build())
        .given(s3Client).deleteObject(any(DeleteObjectRequest.class));

    // when, then
    assertThatThrownBy(() -> storage.delete(url))
        .isInstanceOf(S3DeleteException.class);
  }

}
