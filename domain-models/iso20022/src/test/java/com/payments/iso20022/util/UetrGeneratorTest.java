package com.payments.iso20022.util;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for UetrGenerator */
@DisplayName("UETR Generator Tests")
class UetrGeneratorTest {

  @Test
  @DisplayName("Should generate valid UUID v4 UETR")
  void shouldGenerateValidUuidV4Uetr() {
    // When
    String uetr = UetrGenerator.generate();

    // Then
    assertThat(uetr).isNotNull();
    assertThat(uetr).hasSize(36); // 32 hex chars + 4 hyphens
    assertThat(uetr)
        .matches("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
    assertThat(UetrGenerator.isValid(uetr)).isTrue();
  }

  @Test
  @DisplayName("Should generate unique UETRs")
  void shouldGenerateUniqueUetrs() {
    // When - Generate 1000 UETRs
    Set<String> uetrs = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      uetrs.add(UetrGenerator.generate());
    }

    // Then - All should be unique
    assertThat(uetrs).hasSize(1000);
  }

  @Test
  @DisplayName("Should generate UETR with context")
  void shouldGenerateUetrWithContext() {
    // When
    String uetr = UetrGenerator.generateWithContext("SAMOS");

    // Then
    assertThat(uetr).isNotNull();
    assertThat(UetrGenerator.isValid(uetr)).isTrue();
  }

  @Test
  @DisplayName("Should generate batch of UETRs")
  void shouldGenerateBatchOfUetrs() {
    // When
    String[] uetrs = UetrGenerator.generateBatch(100);

    // Then
    assertThat(uetrs).hasSize(100);
    assertThat(uetrs).doesNotContainNull();

    // All should be valid and unique
    Set<String> uniqueUetrs = new HashSet<>();
    for (String uetr : uetrs) {
      assertThat(UetrGenerator.isValid(uetr)).isTrue();
      uniqueUetrs.add(uetr);
    }
    assertThat(uniqueUetrs).hasSize(100);
  }

  @Test
  @DisplayName("Should reject invalid batch count")
  void shouldRejectInvalidBatchCount() {
    // Then
    assertThatThrownBy(() -> UetrGenerator.generateBatch(0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Count must be between 1 and 10000");

    assertThatThrownBy(() -> UetrGenerator.generateBatch(-1))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> UetrGenerator.generateBatch(10001))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Should validate correct UETR format")
  void shouldValidateCorrectUetrFormat() {
    // Given
    String validUetr = "550e8400-e29b-41d4-a716-446655440000";

    // When/Then
    assertThat(UetrGenerator.isValid(validUetr)).isTrue();
  }

  @Test
  @DisplayName("Should reject invalid UETR formats")
  void shouldRejectInvalidUetrFormats() {
    // Given - Various invalid formats
    String[] invalidUetrs = {
      null,
      "",
      "not-a-uuid",
      "550e8400-e29b-41d4-a716", // Too short
      "550e8400-e29b-41d4-a716-446655440000-extra", // Too long
      "550e8400-e29b-31d4-a716-446655440000", // Wrong version (3 instead of 4)
      "550e8400-e29b-41d4-1716-446655440000", // Invalid variant (1 instead of 8/9/a/b)
      "550e8400-e29b-41d4-a716-44665544000g", // Invalid hex char (g)
      "550e8400e29b41d4a716446655440000" // Missing hyphens
    };

    // When/Then
    for (String invalid : invalidUetrs) {
      assertThat(UetrGenerator.isValid(invalid)).as("Should reject: %s", invalid).isFalse();
    }
  }

  @Test
  @DisplayName("Should accept both uppercase and lowercase UETRs")
  void shouldAcceptBothUppercaseAndLowercaseUetrs() {
    // Given
    String lowercase = "550e8400-e29b-41d4-a716-446655440000";
    String uppercase = "550E8400-E29B-41D4-A716-446655440000";
    String mixed = "550e8400-E29B-41d4-A716-446655440000";

    // When/Then - All should be valid
    assertThat(UetrGenerator.isValid(lowercase)).isTrue();
    assertThat(UetrGenerator.isValid(uppercase)).isTrue();
    assertThat(UetrGenerator.isValid(mixed)).isTrue();
  }

  @Test
  @DisplayName("Should validate any UUID version with lenient validator")
  void shouldValidateAnyUuidVersionWithLenientValidator() {
    // Given
    String uuidV1 = "550e8400-e29b-11d4-a716-446655440000"; // Version 1
    String uuidV3 = "550e8400-e29b-31d4-a716-446655440000"; // Version 3
    String uuidV4 = "550e8400-e29b-41d4-a716-446655440000"; // Version 4
    String uuidV5 = "550e8400-e29b-51d4-a716-446655440000"; // Version 5

    // When/Then - Strict validator only accepts v4
    assertThat(UetrGenerator.isValid(uuidV1)).isFalse();
    assertThat(UetrGenerator.isValid(uuidV3)).isFalse();
    assertThat(UetrGenerator.isValid(uuidV4)).isTrue();
    assertThat(UetrGenerator.isValid(uuidV5)).isFalse();

    // Lenient validator accepts all versions
    assertThat(UetrGenerator.isValidUuid(uuidV1)).isTrue();
    assertThat(UetrGenerator.isValidUuid(uuidV3)).isTrue();
    assertThat(UetrGenerator.isValidUuid(uuidV4)).isTrue();
    assertThat(UetrGenerator.isValidUuid(uuidV5)).isTrue();
  }

  @Test
  @DisplayName("Should validate or throw exception")
  void shouldValidateOrThrowException() {
    // Given
    String validUetr = "550e8400-e29b-41d4-a716-446655440000";
    String invalidUetr = "invalid";

    // When/Then - Valid UETR should not throw
    assertThatCode(() -> UetrGenerator.validateOrThrow(validUetr)).doesNotThrowAnyException();

    // Invalid UETR should throw
    assertThatThrownBy(() -> UetrGenerator.validateOrThrow(invalidUetr))
        .isInstanceOf(UetrGenerator.InvalidUetrException.class)
        .hasMessageContaining("Invalid UETR format");
  }

  @Test
  @DisplayName("Should extract UUID version")
  void shouldExtractUuidVersion() {
    // Given
    String uuidV4 = "550e8400-e29b-41d4-a716-446655440000";

    // When
    int version = UetrGenerator.getVersion(uuidV4);

    // Then
    assertThat(version).isEqualTo(4);
  }

  @Test
  @DisplayName("Should extract UUID variant")
  void shouldExtractUuidVariant() {
    // Given
    String uetr = "550e8400-e29b-41d4-a716-446655440000";

    // When
    int variant = UetrGenerator.getVariant(uetr);

    // Then - Variant should be 8, 9, A, or B (hex: 10, 10, 10, or 11 in binary variant bits)
    assertThat(variant).isBetween(8, 11);
  }

  @Test
  @DisplayName("Should format UETR with hyphens")
  void shouldFormatUetrWithHyphens() {
    // Given - UETR without hyphens
    String compact = "550e8400e29b41d4a716446655440000";

    // When
    String formatted = UetrGenerator.format(compact);

    // Then
    assertThat(formatted).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    assertThat(UetrGenerator.isValid(formatted)).isTrue();
  }

  @Test
  @DisplayName("Should handle uppercase in format")
  void shouldHandleUppercaseInFormat() {
    // Given - Uppercase UETR
    String uppercase = "550E8400-E29B-41D4-A716-446655440000";

    // When
    String formatted = UetrGenerator.format(uppercase);

    // Then - Should convert to lowercase
    assertThat(formatted).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
  }

  @Test
  @DisplayName("Should reject invalid format input")
  void shouldRejectInvalidFormatInput() {
    // Then
    assertThatThrownBy(() -> UetrGenerator.format(null))
        .isInstanceOf(UetrGenerator.InvalidUetrException.class)
        .hasMessageContaining("cannot be null or empty");

    assertThatThrownBy(() -> UetrGenerator.format(""))
        .isInstanceOf(UetrGenerator.InvalidUetrException.class);

    assertThatThrownBy(() -> UetrGenerator.format("too-short"))
        .isInstanceOf(UetrGenerator.InvalidUetrException.class)
        .hasMessageContaining("Invalid UETR length");
  }

  @Test
  @DisplayName("Should convert to uppercase")
  void shouldConvertToUppercase() {
    // Given
    String uetr = "550e8400-e29b-41d4-a716-446655440000";

    // When
    String uppercase = UetrGenerator.toUpperCase(uetr);

    // Then
    assertThat(uppercase).isEqualTo("550E8400-E29B-41D4-A716-446655440000");
  }

  @Test
  @DisplayName("Should convert to lowercase")
  void shouldConvertToLowercase() {
    // Given
    String uetr = "550E8400-E29B-41D4-A716-446655440000";

    // When
    String lowercase = UetrGenerator.toLowerCase(uetr);

    // Then
    assertThat(lowercase).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
  }

  @Test
  @DisplayName("Should convert to compact format")
  void shouldConvertToCompactFormat() {
    // Given
    String uetr = "550e8400-e29b-41d4-a716-446655440000";

    // When
    String compact = UetrGenerator.toCompactFormat(uetr);

    // Then
    assertThat(compact).isEqualTo("550e8400e29b41d4a716446655440000");
    assertThat(compact).hasSize(32);
  }

  @Test
  @DisplayName("Should convert from UUID object")
  void shouldConvertFromUuidObject() {
    // Given
    UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    // When
    String uetr = UetrGenerator.fromUuid(uuid);

    // Then
    assertThat(uetr).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    assertThat(UetrGenerator.isValid(uetr)).isTrue();
  }

  @Test
  @DisplayName("Should reject null UUID object")
  void shouldRejectNullUuidObject() {
    // Then
    assertThatThrownBy(() -> UetrGenerator.fromUuid(null))
        .isInstanceOf(UetrGenerator.InvalidUetrException.class)
        .hasMessageContaining("UUID cannot be null");
  }

  @Test
  @DisplayName("Should convert to UUID object")
  void shouldConvertToUuidObject() {
    // Given
    String uetr = "550e8400-e29b-41d4-a716-446655440000";

    // When
    UUID uuid = UetrGenerator.toUuid(uetr);

    // Then
    assertThat(uuid).isNotNull();
    assertThat(uuid.toString()).isEqualTo(uetr);
  }

  @Test
  @DisplayName("Should compare UETRs case-insensitively")
  void shouldCompareUetrsCaseInsensitively() {
    // Given
    String uetr1 = "550e8400-e29b-41d4-a716-446655440000";
    String uetr2 = "550E8400-E29B-41D4-A716-446655440000";
    String different = "660e8400-e29b-41d4-a716-446655440000";

    // When/Then
    assertThat(UetrGenerator.equals(uetr1, uetr2)).isTrue();
    assertThat(UetrGenerator.equals(uetr1, different)).isFalse();
    assertThat(UetrGenerator.equals(uetr1, null)).isFalse();
    assertThat(UetrGenerator.equals(null, uetr2)).isFalse();
  }

  @Test
  @DisplayName("Should work with real SWIFT gpi UETR examples")
  void shouldWorkWithRealSwiftGpiUetrExamples() {
    // Given - Real SWIFT gpi UETR examples
    String[] realUetrs = {
      "97ed4827-7b6f-4491-a06f-b548d5a8d98f",
      "b4430278-c5ce-4a19-a8f3-0e4348643ff4",
      "e1fc03c8-d1e3-4e6f-9c3e-3b2f8e5a7c8d",
      "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    };

    // When/Then - All should be valid
    for (String uetr : realUetrs) {
      assertThat(UetrGenerator.isValid(uetr))
          .as("Real SWIFT UETR should be valid: %s", uetr)
          .isTrue();
    }
  }

  @Test
  @DisplayName("Should handle round-trip conversion")
  void shouldHandleRoundTripConversion() {
    // Given
    String original = UetrGenerator.generate();

    // When - Convert to UUID and back
    UUID uuid = UetrGenerator.toUuid(original);
    String roundTrip = UetrGenerator.fromUuid(uuid);

    // Then
    assertThat(roundTrip).isEqualTo(original);
  }

  @Test
  @DisplayName("Should handle format round-trip conversion")
  void shouldHandleFormatRoundTripConversion() {
    // Given
    String original = UetrGenerator.generate();

    // When - Convert to compact and back
    String compact = UetrGenerator.toCompactFormat(original);
    String formatted = UetrGenerator.format(compact);

    // Then
    assertThat(formatted).isEqualTo(original);
  }
}
