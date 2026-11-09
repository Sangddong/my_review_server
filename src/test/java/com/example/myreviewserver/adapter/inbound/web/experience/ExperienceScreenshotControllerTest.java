package com.example.myreviewserver.adapter.inbound.web.experience;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.adapter.inbound.security.JwtTokenProvider;
import com.example.myreviewserver.application.experience.ExtractedExperienceDraft;
import com.example.myreviewserver.application.experience.VisionExperienceExtractor;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExperienceScreenshotControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PlatformRepository platformRepository;

	@MockitoBean
	VisionExperienceExtractor visionExperienceExtractor;

	@Test
	void requiresAuthentication() throws Exception {
		mockMvc.perform(multipart("/api/experiences/from-screenshot")
				.file(pngFile()))
			.andExpect(status().isForbidden());
	}

	@Test
	void returnsExtractedFieldsWithoutReservation() throws Exception {
		User user = userRepository.save(User.create("shot@test.com", "shotUser"));
		Platform blog = platformRepository.save(Platform.create(user.getId(), "네이버 블로그", "#03c75a", 0));
		Platform clip = platformRepository.save(Platform.create(user.getId(), "클립", "#00c73c", 1));
		String jwt = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		when(visionExperienceExtractor.extract(any(), any())).thenReturn(new ExtractedExperienceDraft(
			"버질스퀘어 성수",
			ExperienceType.VISIT,
			LocalDate.of(2026, 11, 13),
			List.of("네이버 블로그", "클립"),
			"https://www.reviewnote.co.kr/campaigns/1489015"
		));

		mockMvc.perform(multipart("/api/experiences/from-screenshot")
				.file(pngFile())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.name").value("버질스퀘어 성수"))
			.andExpect(jsonPath("$.data.experienceType").value("VISIT"))
			.andExpect(jsonPath("$.data.reviewDeadline").value("2026-11-13"))
			.andExpect(jsonPath("$.data.platformNames[0]").value("네이버 블로그"))
			.andExpect(jsonPath("$.data.platformNames[1]").value("클립"))
			.andExpect(jsonPath("$.data.platformIds[0]").value(blog.getId()))
			.andExpect(jsonPath("$.data.platformIds[1]").value(clip.getId()))
			.andExpect(jsonPath("$.data.detailLink").value("https://www.reviewnote.co.kr/campaigns/1489015"))
			.andExpect(jsonPath("$.data.reservationDate").doesNotExist())
			.andExpect(jsonPath("$.data.reservationTime").doesNotExist());
	}

	@Test
	void rejectsAnEmptyFileBeforeCallingTheModel() throws Exception {
		User user = userRepository.save(User.create("shot-empty@test.com", "emptyUser"));
		String jwt = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		MockMultipartFile empty = new MockMultipartFile("image", "shot.png", "image/png", new byte[0]);

		mockMvc.perform(multipart("/api/experiences/from-screenshot")
				.file(empty)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("image is required"));

		verify(visionExperienceExtractor, never()).extract(any(), any());
	}

	@Test
	void rejectsAFileThatIsNotAnImage() throws Exception {
		User user = userRepository.save(User.create("shot-bad@test.com", "badUser"));
		String jwt = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		MockMultipartFile text = new MockMultipartFile("image", "note.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

		mockMvc.perform(multipart("/api/experiences/from-screenshot")
				.file(text)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("unsupported image"));

		verify(visionExperienceExtractor, never()).extract(any(), any());
	}

	private static MockMultipartFile pngFile() throws Exception {
		BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "png", out);
		return new MockMultipartFile("image", "shot.png", "image/png", out.toByteArray());
	}
}
