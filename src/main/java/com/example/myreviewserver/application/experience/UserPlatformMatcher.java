package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.platform.Platform;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Attaches the signed-in user's platform ids to labels the model returned.
 * The model compares words. Ids stay here because a wrong digit would select the wrong tag.
 */
final class UserPlatformMatcher {

	private static final List<String> CHANNEL_TOKENS = List.of(
		"인스타그램",
		"블로그",
		"유튜브",
		"숏츠",
		"쇼츠",
		"릴스",
		"클립",
		"틱톡",
		"인스타",
		"구글맵",
		"영수증"
	).stream().map(UserPlatformMatcher::normalize).toList();

	private UserPlatformMatcher() {
	}

	static List<Long> matchIds(List<Platform> catalog, List<String> names) {
		if (catalog == null || catalog.isEmpty() || names == null || names.isEmpty()) {
			return List.of();
		}
		Set<Long> used = new LinkedHashSet<>();
		List<Long> ids = new ArrayList<>();
		for (String raw : names) {
			if (raw == null) {
				continue;
			}
			String needle = normalize(raw);
			if (needle.isEmpty()) {
				continue;
			}
			Platform exact = find(catalog, used, platform -> normalize(platform.getName()).equals(needle));
			if (exact != null) {
				add(used, ids, exact);
				continue;
			}
			Platform contained = find(catalog, used, platform -> {
				String name = normalize(platform.getName());
				return name.length() >= 2 && needle.contains(name);
			});
			if (contained != null) {
				add(used, ids, contained);
				continue;
			}
			String token = CHANNEL_TOKENS.stream()
				.filter(channel -> needle.equals(channel) || needle.contains(channel))
				.findFirst()
				.orElse(null);
			if (token == null) {
				continue;
			}
			for (Platform platform : catalog) {
				if (platform.getId() == null || used.contains(platform.getId())) {
					continue;
				}
				if (!normalize(platform.getName()).contains(token)) {
					continue;
				}
				add(used, ids, platform);
			}
		}
		return List.copyOf(ids);
	}

	private static Platform find(List<Platform> catalog, Set<Long> used, Predicate<Platform> predicate) {
		for (Platform platform : catalog) {
			if (platform.getId() == null || used.contains(platform.getId())) {
				continue;
			}
			if (predicate.test(platform)) {
				return platform;
			}
		}
		return null;
	}

	private static void add(Set<Long> used, List<Long> ids, Platform platform) {
		used.add(platform.getId());
		ids.add(platform.getId());
	}

	private static String normalize(String value) {
		return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
	}
}
