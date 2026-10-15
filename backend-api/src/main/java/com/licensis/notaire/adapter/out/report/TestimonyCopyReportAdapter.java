package com.licensis.notaire.adapter.out.report;

import com.licensis.notaire.application.port.out.report.ReportDocument;
import com.licensis.notaire.application.port.out.report.ReportDocument.Field;
import com.licensis.notaire.application.port.out.report.ReportDocument.Fields;
import com.licensis.notaire.application.port.out.report.ReportDocument.Identity;
import com.licensis.notaire.application.port.out.report.TestimonyCopyReportPort;
import com.licensis.notaire.application.usecase.report.ReportLocaleFormats;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.TestimonyRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the CU08 testimony-copy {@link ReportDocument} from a verified testimony.
 */
@Component
public class TestimonyCopyReportAdapter implements TestimonyCopyReportPort {

    private final TestimonyRepository testimonyRepository;

    public TestimonyCopyReportAdapter(TestimonyRepository testimonyRepository) {
        this.testimonyRepository = testimonyRepository;
    }

    @Override
    public ReportDocument build(Integer idTestimony) {
        Testimony testimony = testimonyRepository.findById(idTestimony)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Testimony not found with ID: " + idTestimony));
        if (!testimony.getVerified()) {
            throw new BusinessValidationException(
                    "The testimony must be verified to issue the printed copy");
        }

        Deed deed = testimony.getFkIdDeed();
        List<Copy> copies = testimony.getCopyList();
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("Testimony", String.valueOf(testimony.getNumber())));
        fields.add(new Field("Verified", "Yes"));
        fields.add(new Field("Observed", testimony.getFlagged() ? "Yes" : "No"));
        fields.add(new Field("Notes", notes(testimony)));
        fields.add(new Field("Copies", copies == null
                ? ReportLocaleFormats.NOT_RECORDED
                : String.valueOf(copies.size())));
        fields.add(new Field("Deed", deed == null
                ? ReportLocaleFormats.NOT_RECORDED
                : String.valueOf(deed.getNumber())));
        fields.add(new Field("Deed date", deed == null
                ? ReportLocaleFormats.NOT_RECORDED
                : ReportLocaleFormats.day(deed.getDateDeedrecording())));
        fields.add(new Field("Deed body", deed == null
                ? ReportLocaleFormats.NOT_RECORDED
                : ReportLocaleFormats.fallback(deed.getBody())));

        return new ReportDocument(
                "Testimony copy",
                "CU08",
                List.of(new Fields("Testimony", fields)),
                new Identity("CU08", "testimony-" + testimony.getNumber(), "Verified testimony",
                        null, ReportDocument.PageOrientation.PORTRAIT));
    }

    private static String notes(Testimony testimony) {
        if (!testimony.getFlagged()) {
            return ReportLocaleFormats.NOT_RECORDED;
        }
        return ReportLocaleFormats.fallback(testimony.getNotes());
    }
}
