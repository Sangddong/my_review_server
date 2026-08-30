package com.example.myreviewserver.config;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Retention policy for hard-deleting soft-deleted notifications.
 * The zone drives both the cron trigger and the retention cutoff.
 *
 * @ConfigurationProperties: app.notification.purge.* 설정을 이 클래스 필드에 바인딩.
 */
@ConfigurationProperties(prefix = "app.notification.purge")
public class NotificationPurgeProperties {

	private int afterMonths = 1;

	private String cron = "0 0 4 * * *";

	private String zone = "Asia/Seoul";

	public int getAfterMonths() {
		return afterMonths;
	}

	public void setAfterMonths(int afterMonths) {
		this.afterMonths = afterMonths;
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
