package com.licensis.notaire.adapter.in.web.audit;

import com.licensis.notaire.application.port.out.audit.AuditRecordOpsPort;
import com.licensis.notaire.application.port.out.audit.AuditRecordOpsPort.PageResult;
import com.licensis.notaire.dto.DtoAuditRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit-log")
@Tag(name = "AuditRecord", description = "API for querying user audit records (read only; mutations are denied)")
public class AuditRecordController {

    private final AuditRecordOpsPort ops;

    public AuditRecordController(AuditRecordOpsPort ops) {
        this.ops = ops;
    }

    /**
     * Returns audit log entries. Supports pagination, sort and optional
     * filters by {@code module}, {@code idUser} and an inclusive {@code from}/{@code to}
     * calendar-day range (#1545 / CU23); the filters combine. When no pagination
     * parameters are present the response is a flat list of DTOs (kept for
     * backwards compatibility with the existing Swing/Next clients).
     */
    @GetMapping
    @Operation(summary = "Get audit records with pagination and optional filters")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "date,desc") String sort,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer idUser,
            @Parameter(description = "First day, inclusive (yyyy-MM-dd, business time zone)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Last day, inclusive (yyyy-MM-dd, business time zone)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        boolean paginated = page != null || size != null;
        if (!paginated) {
            return ResponseEntity.ok(ops.findAll());
        }

        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : size;
        PageResult result = ops.search(module, idUser, from, to, safePage, safeSize, sort);
        return ResponseEntity.ok(toPageResponse(result));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get an audit record by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoAuditRecord> getById(@PathVariable Integer id) {
        return ops.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{idUser}")
    @Operation(summary = "Get audit records by user")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoAuditRecord>> getByUser(@PathVariable Integer idUser) {
        return ResponseEntity.ok(ops.findByUserId(idUser));
    }

    private Map<String, Object> toPageResponse(PageResult page) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", page.content());
        response.put("page", page.number());
        response.put("size", page.size());
        response.put("totalElements", page.totalElements());
        response.put("totalPages", page.totalPages());
        response.put("first", page.first());
        response.put("last", page.last());
        return response;
    }
}
