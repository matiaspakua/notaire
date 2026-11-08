package com.licensis.notaire.application.usecase.report;

import com.licensis.notaire.adapter.out.report.TestimonyCopyReportAdapter;
import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.TestimonyRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU08"})
@DisplayName("TestimonyCopyReportAdapter CU08 rows (#119)")
@ExtendWith(MockitoExtension.class)
class TestimonyCopyReportAdapterTest {

    @Mock
    private TestimonyRepository testimonyRepository;

    @Test
    @DisplayName("verified testimony prints recorded deed body")
    void shouldPrintRecordedDeedBody() {
        when(testimonyRepository.findById(7)).thenReturn(Optional.of(verified(
                42, false, null, deed(881, "Signed deed for a testimony copy"), List.of())));

        ReportDocument document = adapter().build(7);
        Fields fields = (Fields) document.sections().getFirst();

        assertThat(document.title()).isEqualTo("Testimony copy");
        assertThat(document.subtitle()).isEqualTo("CU08");
        assertThat(value(fields, "Testimony")).isEqualTo("42");
        assertThat(value(fields, "Deed")).isEqualTo("881");
        assertThat(value(fields, "Deed body")).isEqualTo("Signed deed for a testimony copy");
        assertThat(fields.fields().stream().map(Field::value).toList())
                .noneMatch(value -> value != null && value.toLowerCase().contains("signature"));
    }

    @Test
    @DisplayName("missing deed body stays N/A")
    void shouldUseNaWhenDeedBodyMissing() {
        when(testimonyRepository.findById(8)).thenReturn(Optional.of(verified(
                9, false, null, deed(12, null), List.of())));

        Fields fields = (Fields) adapter().build(8).sections().getFirst();

        assertThat(value(fields, "Testimony")).isEqualTo("9");
        assertThat(value(fields, "Deed body")).isEqualTo(ReportLocaleFormats.NOT_RECORDED);
    }

    @Test
    @DisplayName("observed notes appear when recorded")
    void shouldPrintObservedNotes() {
        when(testimonyRepository.findById(3)).thenReturn(Optional.of(verified(
                5, true, "Missing grantor signature", deed(20, "Body"), List.of(new Copy()))));

        Fields fields = (Fields) adapter().build(3).sections().getFirst();

        assertThat(value(fields, "Notes")).isEqualTo("Missing grantor signature");
        assertThat(value(fields, "Copies")).isEqualTo("1");
        assertThat(value(fields, "Observed")).isEqualTo("Yes");
    }

    @Test
    @DisplayName("unverified testimony is rejected")
    void shouldRejectUnverified() {
        Testimony unverified = new Testimony();
        unverified.setIdTestimony(4);
        unverified.setNumber(11);
        unverified.setVerified(false);
        when(testimonyRepository.findById(4)).thenReturn(Optional.of(unverified));

        assertThatThrownBy(() -> adapter().build(4))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("verified");
    }

    @Test
    @DisplayName("unknown testimony is not found")
    void shouldRejectUnknown() {
        when(testimonyRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter().build(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private TestimonyCopyReportAdapter adapter() {
        return new TestimonyCopyReportAdapter(testimonyRepository);
    }

    private static Testimony verified(int number, boolean observed, String notes, Deed deed, List<Copy> copies) {
        Testimony testimony = new Testimony();
        testimony.setIdTestimony(1);
        testimony.setNumber(number);
        testimony.setVerified(true);
        testimony.setFlagged(observed);
        testimony.setNotes(notes);
        testimony.setFkIdDeed(deed);
        testimony.setCopyList(copies);
        return testimony;
    }

    private static Deed deed(int number, String body) {
        Deed deed = new Deed();
        deed.setNumber(number);
        deed.setBody(body);
        deed.setDateDeedrecording(Date.valueOf(LocalDate.of(2026, 6, 16)));
        return deed;
    }

    private static String value(Fields fields, String label) {
        return fields.fields().stream()
                .filter(field -> label.equals(field.label()))
                .map(Field::value)
                .findFirst()
                .orElseThrow();
    }
}
