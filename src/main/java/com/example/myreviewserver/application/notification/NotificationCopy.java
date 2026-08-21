package com.example.myreviewserver.application.notification;

/**
 * Title/body templates for notification rules. Experience name is included in titles.
 */
public final class NotificationCopy {

	private NotificationCopy() {
	}

	public static String d3Title(String experienceName) {
		return requiredName(experienceName) + " 리뷰 마감 3일 전입니다";
	}

	public static String d3Body() {
		return "마감일 전에 리뷰를 작성하여 제출해주세요";
	}

	public static String todayTitle(String experienceName) {
		return requiredName(experienceName) + " 오늘 체험 일정이 있어요";
	}

	public static String todayBody() {
		return "오늘 체험할 일정을 확인해보세요";
	}

	public static String overdueTitle(String experienceName) {
		return requiredName(experienceName) + " 리뷰 제출 기한이 초과되었습니다";
	}

	public static String overdueBody() {
		return "리뷰 제출을 서둘러주세요";
	}

	private static String requiredName(String experienceName) {
		if (experienceName == null || experienceName.isBlank()) {
			return "체험";
		}
		return experienceName.trim();
	}
}
