package com.example.myreviewserver.adapter.inbound.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves public legal HTML pages for Play/App Store and in-app links (#103).
 *
 * @Controller: 뷰(HTML forward)를 반환하는 컨트롤러.
 * @GetMapping: HTTP GET 경로 매핑.
 */
@Controller
public class LegalDocumentsController {

	@GetMapping({"/legal/privacy", "/privacy"})
	public String privacy() {
		return "forward:/legal/privacy.html";
	}

	@GetMapping({"/legal/terms", "/terms"})
	public String terms() {
		return "forward:/legal/terms.html";
	}
}
