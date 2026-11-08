package com.licensis.notaire.adapter.in.web.support;

import jakarta.validation.constraints.NotBlank;

public record NotaryRef(
        Integer personId,
        String firstName,
        @NotBlank String lastName,
        @NotBlank String identificationNumber,
        Integer notaryRegistrationNumber) {}
