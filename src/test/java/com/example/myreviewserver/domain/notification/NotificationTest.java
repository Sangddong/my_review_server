package com.example.myreviewserver.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

class NotificationTest {

	@Test
	void createsUnreadAndMarksRead() {
		Notification notification = Notification.create(1L, 10L, "D3", "제목", "본문");
		assertThat(notification.isRead()).isFalse();
		assertThat(notification.getIsRead()).isNull();

		notification.markRead();
		assertThat(notification.isRead()).isTrue();
		assertThat(notification.getIsRead()).isEqualTo(1);
	}

	@Test
	void rejectsBlankTitle() {
		assertThatThrownBy(() -> Notification.create(1L, 10L, "D3", " ", "본문"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("title");
	}
}
