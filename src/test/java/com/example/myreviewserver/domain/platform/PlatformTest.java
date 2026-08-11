package com.example.myreviewserver.domain.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

class PlatformTest {

	@Test
	void createAndSoftDelete() {
		Platform platform = Platform.create(1L, "블로그", "#c6f8c8", 0);

		assertThat(platform.isActive()).isTrue();
		assertThat(platform.getIsDeleted()).isNull();

		platform.softDelete();

		assertThat(platform.isActive()).isFalse();
		assertThat(platform.getIsDeleted()).isEqualTo(1);
		assertThatThrownBy(() -> platform.rename("블로그2"))
			.isInstanceOf(DomainException.class);
	}

	@Test
	void rejectsNonHexColor() {
		assertThatThrownBy(() -> Platform.create(1L, "블로그", "var(--color-chip-blog)", 0))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("hex");
	}

	@Test
	void expandsShortHexColor() {
		Platform platform = Platform.create(1L, "블로그", "#c8f", 0);
		assertThat(platform.getColor()).isEqualTo("#cc88ff");
	}
}
