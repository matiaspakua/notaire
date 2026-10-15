package com.licensis.notaire.adapter.in.web.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;

import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * #1712: FK conflict on delete answers 409 and must log WARN, not ERROR.
 */
class ManagementControllerDeleteLogTest {

    private Logger controllerLogger;
    private Level originalLevel;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        controllerLogger = (Logger) LoggerFactory.getLogger(ManagementController.class);
        originalLevel = controllerLogger.getLevel();
        controllerLogger.setLevel(Level.TRACE);
        appender = new ListAppender<>();
        appender.start();
        controllerLogger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        controllerLogger.detachAppender(appender);
        controllerLogger.setLevel(originalLevel);
    }

    @Test
    @DisplayName("FK conflict on delete answers 409 and logs WARN")
    void deleteConflictIsWarn() throws Exception {
        DeedManagementRepository repo = mock(DeedManagementRepository.class);
        when(repo.existsById(5)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk")).when(repo).deleteById(anyInt());

        MockMvc mvc = standaloneSetup(new ManagementController(
                repo,
                mock(HistoryRepository.class),
                mock(com.licensis.notaire.application.usecase.workflow.WorkflowTraceService.class),
                mock(com.licensis.notaire.application.usecase.management.ManagementQueryService.class),
                mock(com.licensis.notaire.repository.PersonRepository.class),
                mock(com.licensis.notaire.repository.ManagementStatusRepository.class),
                mock(com.licensis.notaire.repository.BudgetRepository.class),
                mock(com.licensis.notaire.repository.ProcedureTypeRepository.class),
                mock(com.licensis.notaire.repository.ProcedureRepository.class),
                mock(com.licensis.notaire.repository.PropertyRepository.class),
                mock(com.licensis.notaire.application.usecase.management.ManagementArchiveDebtService.class),
                mock(com.licensis.notaire.application.usecase.management.ManagementSubstitutionService.class),
                mock(com.licensis.notaire.application.usecase.management.ManagementFinancialSummaryService.class),
                mock(com.licensis.notaire.application.usecase.management.ManagementStatusLogService.class),
                mock(com.licensis.notaire.application.usecase.management.ManagementStatusWriteGuard.class),
                mock(com.licensis.notaire.application.port.in.management.TransitionManagementUseCase.class),
                mock(TransitionManagementWebMapper.class),
                mock(com.licensis.notaire.application.usecase.document.ExternalEntityDocumentService.class),
                mock(com.licensis.notaire.application.usecase.workflow.DocumentReentryService.class),
                mock(com.licensis.notaire.application.usecase.procedure.ProcedureFolderService.class),
                mock(com.licensis.notaire.application.usecase.procedure.RequiredDocumentationOnConfirmService.class)))
                .build();

        mvc.perform(delete("/api/v1/managements/5")).andExpect(status().isConflict());

        List<ILoggingEvent> deleteLogs = appender.list.stream()
                .filter(e -> e.getFormattedMessage().contains("Failed to delete management"))
                .toList();
        assertThat(deleteLogs).isNotEmpty();
        assertThat(deleteLogs.get(deleteLogs.size() - 1).getLevel()).isEqualTo(Level.WARN);
    }
}
