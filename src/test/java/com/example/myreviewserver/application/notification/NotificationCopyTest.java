package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotificationCopyTest {

	@Test
	void buildsTitlesAndBodiesWithExperienceName() {
		assertThat(NotificationCopy.d3Title("넥쿨러")).isEqualTo("넥쿨러 리뷰 마감 3일 전입니다");
		assertThat(NotificationCopy.d3Body()).isEqualTo("마감일 전에 리뷰를 작성하여 제출해주세요");

		assertThat(NotificationCopy.todayTitle("넥쿨러")).isEqualTo("넥쿨러 오늘 체험 일정이 있어요");
		assertThat(NotificationCopy.todayBody()).isEqualTo("오늘 체험할 일정을 확인해보세요");

		assertThat(NotificationCopy.overdueTitle("넥쿨러")).isEqualTo("넥쿨러 리뷰 제출 기한이 초과되었습니다");
		assertThat(NotificationCopy.overdueBody()).isEqualTo("리뷰 제출을 서둘러주세요");
	}

	@Test
	void fallsBackWhenNameBlank() {
		assertThat(NotificationCopy.d3Title(" ")).isEqualTo("체험 리뷰 마감 3일 전입니다");
		assertThat(NotificationCopy.todayTitle(null)).isEqualTo("체험 오늘 체험 일정이 있어요");
	}
}
