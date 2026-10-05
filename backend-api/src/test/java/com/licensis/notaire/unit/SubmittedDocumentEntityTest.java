package com.licensis.notaire.unit;

import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.testing.RequirementCoverage;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@RequirementCoverage({"CU42", "CU50", "CU56", "CU72"})
@DisplayName("SubmittedDocument Entity Tests")
class SubmittedDocumentEntityTest {

    @Nested
    @DisplayName("Constructor and default state")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with default constructor")
        void shouldInitializeWithDefaultConstructor() {
            SubmittedDocument doc = new SubmittedDocument();
            assertThat(doc).isNotNull();
        }

        @Test
        @DisplayName("Should initialize with ID constructor")
        void shouldInitializeWithIdConstructor() {
            SubmittedDocument doc = new SubmittedDocument(42);
            assertThat(doc.getIdSubmittedDocument()).isEqualTo(42);
        }

        @Test
        @DisplayName("Should initialize with full constructor")
        void shouldInitializeWithFullConstructor() {
            SubmittedDocument doc = new SubmittedDocument(10, "Doc test", null, false, false, false, false);
            assertThat(doc.getIdSubmittedDocument()).isEqualTo(10);
            assertThat(doc.getName()).isEqualTo("Doc test");
        }
    }

    @Nested
    @DisplayName("Field getters and setters")
    class FieldTests {

        @Test
        @DisplayName("Should set and get name")
        void shouldSetAndGetName() {
            SubmittedDocument doc = new SubmittedDocument();
            doc.setName("Escritura de compraventa");
            assertThat(doc.getName()).isEqualTo("Escritura de compraventa");
        }

        @Test
        @DisplayName("Should set and get notes")
        void shouldSetAndGetNotes() {
            SubmittedDocument doc = new SubmittedDocument();
            doc.setNotes("Requiere notificación al registro");
            assertThat(doc.getNotes()).isEqualTo("Requiere notificación al registro");
        }

        @Test
        @DisplayName("Should set and get flagged")
        void shouldSetAndGetFlagged() {
            SubmittedDocument doc = new SubmittedDocument();
            doc.setFlagged(Boolean.TRUE);
            assertThat(doc.getFlagged()).isTrue();
            doc.setFlagged(Boolean.FALSE);
            assertThat(doc.getFlagged()).isFalse();
        }

        @Test
        @DisplayName("Should set and get released")
        void shouldSetAndGetReleased() {
            SubmittedDocument doc = new SubmittedDocument();
            doc.setReleased(Boolean.TRUE);
            assertThat(doc.getReleased()).isTrue();
        }

        @Test
        @DisplayName("Should set and get delivered (nullable Boolean)")
        void shouldSetAndGetDelivered() {
            SubmittedDocument doc = new SubmittedDocument();
            assertThat(doc.getDelivered()).isNull();
            doc.setDelivered(Boolean.TRUE);
            assertThat(doc.getDelivered()).isTrue();
            doc.setDelivered(Boolean.FALSE);
            assertThat(doc.getDelivered()).isFalse();
        }

        @Test
        @DisplayName("Should set and get reentered (nullable Boolean)")
        void shouldSetAndGetReentered() {
            SubmittedDocument doc = new SubmittedDocument();
            assertThat(doc.getReentered()).isNull();
            doc.setReentered(Boolean.TRUE);
            assertThat(doc.getReentered()).isTrue();
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            SubmittedDocument doc = new SubmittedDocument();
            doc.setVersion(3);
            assertThat(doc.getVersion()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Equality and identity")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            SubmittedDocument d1 = new SubmittedDocument(5);
            SubmittedDocument d2 = new SubmittedDocument(5);
            assertThat(d1).isEqualTo(d2);
            assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            SubmittedDocument d1 = new SubmittedDocument(5);
            SubmittedDocument d2 = new SubmittedDocument(6);
            assertThat(d1).isNotEqualTo(d2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            SubmittedDocument d = new SubmittedDocument(1);
            assertThat(d).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            SubmittedDocument d = new SubmittedDocument(1);
            assertThat(d).isNotEqualTo("string");
        }

        @Test
        @DisplayName("hashCode should be zero when ID is null")
        void hashCodeShouldBeZeroWhenIdIsNull() {
            SubmittedDocument d = new SubmittedDocument();
            assertThat(d.hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("hashCode should be non-zero when ID is set")
        void hashCodeShouldBeNonZeroWhenIdSet() {
            SubmittedDocument d = new SubmittedDocument(99);
            assertThat(d.hashCode()).isNotEqualTo(0);
        }

        @Test
        @DisplayName("Should be equal when both IDs are null")
        void shouldBeEqualWhenBothIdsAreNull() {
            SubmittedDocument d1 = new SubmittedDocument();
            SubmittedDocument d2 = new SubmittedDocument();
            assertThat(d1).isEqualTo(d2);
        }

        @Test
        @DisplayName("Should not be equal when this ID is null and other has ID")
        void shouldNotBeEqualWhenThisIdNullOtherHasId() {
            SubmittedDocument d1 = new SubmittedDocument();
            SubmittedDocument d2 = new SubmittedDocument(5);
            assertThat(d1).isNotEqualTo(d2);
        }

        @Test
        @DisplayName("toString should contain key fields")
        void toStringShouldContainKeyFields() {
            SubmittedDocument doc = new SubmittedDocument(7);
            doc.setCardNumber(101);
            String str = doc.toString();
            assertThat(str).contains("7");
        }
    }

    @Nested
    @DisplayName("DocumentType association (#801 / CU72)")
    class DocumentTypeAssociationTests {

        @Test
        @DisplayName("Should map DocumentType association on SubmittedDocument")
        void shouldMapDocumentTypeAssociation() throws Exception {
            Field field = SubmittedDocument.class.getDeclaredField("documentType");
            assertThat(field.getAnnotation(ManyToOne.class)).isNotNull();
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            assertThat(joinColumn).isNotNull();
            assertThat(joinColumn.name()).isEqualTo("fk_id_document_type");

            DocumentType type = new DocumentType(11);
            type.setName("Title deed");
            SubmittedDocument doc = new SubmittedDocument();
            doc.setDocumentType(type);

            assertThat(doc.getDocumentType()).isSameAs(type);
            assertThat(doc.getFkIdDocumentType()).isEqualTo(11);
            assertThat(doc.getFkIdDocumentTypeNullable()).isEqualTo(11);

            doc.setFkIdDocumentType(22);
            assertThat(doc.getDocumentType()).isNotNull();
            assertThat(doc.getDocumentType().getIdDocumentType()).isEqualTo(22);
            assertThat(doc.getFkIdDocumentTypeNullable()).isEqualTo(22);
        }

        @Test
        @DisplayName("Should declare mappedBy=documentType on DocumentType OneToMany")
        void shouldDeclareMappedByDocumentTypeOnDocumentType() throws Exception {
            Field field = DocumentType.class.getDeclaredField("submittedDocumentCollection");
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            assertThat(oneToMany).isNotNull();
            assertThat(oneToMany.mappedBy()).isEqualTo("documentType");
        }
    }

    @Nested
    @DisplayName("getDto null-guard for optional procedure (#801 / CU72)")
    class GetDtoTests {

    }
}
