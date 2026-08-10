package com.example.myreviewserver.domain.shared;

/**
 * Base type for domain-rule violations.
 * Web/persistence layers must not leak into domain code.
 */
public class DomainException extends RuntimeException {

	public DomainException(String message) {
		super(message);
	}

	public DomainException(String message, Throwable cause) {
		super(message, cause);
	}
}
