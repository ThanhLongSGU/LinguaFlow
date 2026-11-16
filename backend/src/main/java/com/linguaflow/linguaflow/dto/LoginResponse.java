package com.linguaflow.linguaflow.dto;

public record LoginResponse(
                String accessToken,
                String tokenType,
                long expiresIn) {
}