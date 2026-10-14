package com.example.myreviewserver.adapter.inbound.web;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Liveness/smoke endpoint — no auth (#117).
 */
@RestController
@RequestMapping(path = "/api/health", produces = MediaType.APPLICATION_JSON_VALUE)
public class HealthController {

	@GetMapping
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}
}
