package com.example.myreviewserver.application.notification;

/**
 * Payload delivered to a device push token.
 */
public record PushMessage(String title, String body) {
}
