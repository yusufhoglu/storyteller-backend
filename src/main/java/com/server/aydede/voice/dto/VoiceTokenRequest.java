package com.server.aydede.voice.dto;

import jakarta.validation.constraints.NotBlank;

public record VoiceTokenRequest(@NotBlank String name) {

}
