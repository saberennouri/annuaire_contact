package com.sfax2.annuaire;

import static org.assertj.core.api.Assertions.assertThat;

import com.sfax2.annuaire.service.ArabicNormalizer;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArabicNormalizerTest {

    @Test
    void unifiesLetterVariantsAndStripsDiacritics() {
        assertThat(ArabicNormalizer.normalize("الحَنْشَة")).isEqualTo(ArabicNormalizer.normalize("الحنشه"));
        assertThat(ArabicNormalizer.normalize("إبراهيم")).isEqualTo("ابراهيم");
        assertThat(ArabicNormalizer.normalize("مصطفى")).isEqualTo("مصطفي");
    }

    @Test
    void convertsArabicIndicDigits() {
        assertThat(ArabicNormalizer.normalize("٩٧٣٦٢١١٧")).isEqualTo("97362117");
    }

    @Test
    void digitOnlyQueryBecomesSingleToken() {
        assertThat(ArabicNormalizer.tokens("97 362 117")).containsExactly("97362117");
    }

    @Test
    void textQuerySplitsOnWhitespace() {
        assertThat(ArabicNormalizer.tokens("  معهد   الحنشة ")).containsExactly("معهد", "الحنشه");
        assertThat(ArabicNormalizer.tokens(null)).isEqualTo(List.of());
    }
}
