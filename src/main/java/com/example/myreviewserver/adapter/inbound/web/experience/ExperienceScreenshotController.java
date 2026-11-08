package com.example.myreviewserver.adapter.inbound.web.experience;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.experience.ExtractExperienceFromScreenshotUseCase;
import com.example.myreviewserver.config.OpenApiConfig;
import com.example.myreviewserver.domain.shared.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Reads experience form fields from a screenshot. Does not create the experience.
 *
 * @RestController / @RequestMapping / @Tag: HTTP JSON API + Swagger 그룹.
 * @SecurityRequirement: Swagger Authorize(JWT) 필요.
 * @PostMapping: multipart POST만 받음.
 * @RequestPart: 업로드 파일 파트.
 */
@RestController
@RequestMapping("/api/experiences")
@Tag(name = "Experiences", description = "리뷰 체험")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class ExperienceScreenshotController {

	private final ExtractExperienceFromScreenshotUseCase extractExperienceFromScreenshotUseCase;

	public ExperienceScreenshotController(
		ExtractExperienceFromScreenshotUseCase extractExperienceFromScreenshotUseCase
	) {
		this.extractExperienceFromScreenshotUseCase = extractExperienceFromScreenshotUseCase;
	}

	/** POST /api/experiences/from-screenshot */
	@PostMapping(path = "/from-screenshot", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(
		summary = "스크린샷에서 체험 입력값 추출",
		description = """
			로그인한 사용자의 스크린샷에서 이름, 유형, 리뷰 마감일, 플랫폼 이름, 상세 링크만 읽습니다.
			신청 마감과 리뷰 마감을 구분하고, 확신할 수 없는 값은 null입니다.
			예약일과 예약 시각은 추출하지 않습니다. 체험을 저장하지 않으며 이미지도 저장하지 않습니다.
			긴 변은 모델에 보내기 전에 1280px 이하로 줄입니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "추출 성공",
			content = @Content(schema = @Schema(implementation = ExtractedExperienceApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미지가 없거나 추출 실패"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<ExtractedExperienceResponse> fromScreenshot(@RequestPart("image") MultipartFile image) {
		CurrentUser.requireUserId();
		if (image == null || image.isEmpty()) {
			throw new DomainException("image is required");
		}
		byte[] bytes;
		try {
			bytes = image.getBytes();
		}
		catch (IOException ex) {
			throw new DomainException("image is required");
		}
		return ApiResponse.ok(ExtractedExperienceResponse.from(
			extractExperienceFromScreenshotUseCase.extract(bytes)
		));
	}
}
