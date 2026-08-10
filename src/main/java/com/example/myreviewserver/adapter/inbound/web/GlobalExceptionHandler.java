package com.example.myreviewserver.adapter.inbound.web;

import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain/application failures to HTTP responses.
 * Expand with additional exception types as APIs are added.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.fail(ex.getMessage()));
	}
}
