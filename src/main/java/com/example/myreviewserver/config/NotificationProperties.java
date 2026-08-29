package com.example.myreviewserver.config;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cron, timezone, and toggle for the notification dispatcher job.
 * The same zone drives both the cron trigger and the "today" the rules compare against,
 * so the two can never drift onto different calendar days.
 *
 * @ConfigurationProperties: app.notification.* 설정을 이 클래스 필드에 바인딩.
 */
@ConfigurationProperties(prefix = "app.notification")
public class NotificationProperties {

	private boolean enabled = true;

	private String cron = "0 0 9 * * *";

	private String zone = "Asia/Seoul";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public ZoneId getZoneId() {
		return ZoneId.of(zone);
	}
}
