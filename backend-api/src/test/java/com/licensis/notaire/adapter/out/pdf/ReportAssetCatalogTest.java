package com.licensis.notaire.adapter.out.pdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReportAssetCatalog classpath contract (issue #1593)")
class ReportAssetCatalogTest {

    private final ReportAssetCatalog catalog = new ReportAssetCatalog();

    @Test
    @DisplayName("shouldLoadLiberationSansFromClasspath")
    void shouldLoadLiberationSansFromClasspath() {
        byte[] regular = catalog.regularFont();
        byte[] bold = catalog.boldFont();

        assertThat(regular).isNotEmpty();
        assertThat(bold).isNotEmpty();
        assertThat(regular).startsWith(new byte[] {0x00, 0x01, 0x00, 0x00});
        assertThat(catalog.wordmark()).isEqualTo("Notaire");
        assertThat(catalog.approvedLogo()).isEmpty();
    }

    @Test
    @DisplayName("shouldNotLoadTheLegacyHundredPixelLogo")
    void shouldNotLoadTheLegacyHundredPixelLogo() {
        assertThat(ReportAssetCatalog.class.getResource("/pdf/logo/logoRojoLetraMediano.png")).isNull();
        assertThat(getClass().getResource("/docs/images/logoRojoLetraMediano.png")).isNull();
    }
}
