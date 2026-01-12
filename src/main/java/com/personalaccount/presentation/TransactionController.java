package com.personalaccount.presentation;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.domain.transaction.dto.response.TransactionDetailResponse;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.personalaccount.domain.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "거래 관리 API (복식부기)")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "거래 생성",
            description = """
                    새로운 거래를 생성하고 복식부기를 자동으로 처리합니다.
                    
                    **수입**: 차변(결제수단) / 대변(수입 카테고리)
                    **지출**: 차변(지출 카테고리) / 대변(결제수단)
                    
                    대차평형 원칙에 따라 차변과 대변의 합계가 자동으로 일치하도록 검증됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "거래 생성 성공",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 거래 (계정과목 타입 불일치, 장부 타입 불일치 등)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "장부 또는 계정과목을 찾을 수 없습니다"
            )
    })
    @PostMapping
    public ResponseEntity<CommonResponse<TransactionResponse>> createTransaction(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TransactionCreateRequest request) {

        log.info("POST /api/v1/transactions - userId={}", userId);
        TransactionResponse response = transactionService.createTransaction(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseFactory.success(response, "거래 생성 완료"));
    }

    @Operation(
            summary = "거래 목록 조회",
            description = "조건에 따라 거래 목록을 조회합니다. 모든 파라미터는 선택적입니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            )
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<TransactionResponse>>> getTransactions(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestParam Long bookId,
            @RequestParam(required = false) TransactionType type,
            @Parameter(description = "시작일 (선택적)", example = "2025-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일 (선택적)", example = "2025-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /api/v1/transactions - userId={}, bookId={}", userId, bookId);
        List<TransactionResponse> response = transactionService.getTransactions(
                userId, bookId, type, startDate, endDate);
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @Operation(
            summary = "거래 상세 조회",
            description = "거래 ID로 기본 정보를 조회합니다. 복식부기 상세는 포함되지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "거래를 찾을 수 없습니다"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<TransactionResponse>> getTransaction(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        log.info("GET /api/v1/transactions/{} - userId={}", id, userId);
        TransactionResponse response = transactionService.getTransaction(userId, id);
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @Operation(
            summary = "거래 상세 조회 (복식부기 포함)",
            description = """
                    거래 ID로 복식부기 상세 정보를 포함하여 조회합니다.
                    
                    **포함 정보**:
                    - 거래 기본 정보
                    - 분개(JournalEntry) 정보
                    - 분개 상세(TransactionDetail): 차변/대변, 계정과목, 금액
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TransactionDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "거래를 찾을 수 없습니다"
            )
    })
    @GetMapping("/{id}/details")
    public ResponseEntity<CommonResponse<TransactionDetailResponse>> getTransactionDetails(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        log.info("GET /api/v1/transactions/{}/details - userId={}", id, userId);
        TransactionDetailResponse response = transactionService.getTransactionWithDetails(userId, id);
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @Operation(
            summary = "거래 수정",
            description = "거래의 메모만 수정 가능합니다. 금액이나 계정과목은 수정할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "거래를 찾을 수 없습니다"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<TransactionResponse>> updateTransaction(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request) {

        log.info("PUT /api/v1/transactions/{} - userId={}", id, userId);
        TransactionResponse response = transactionService.updateTransaction(userId, id, request);
        return ResponseEntity.ok(ResponseFactory.success(response, "거래 수정 완료"));
    }

    @Operation(
            summary = "거래 삭제",
            description = "거래를 비활성화합니다 (Soft Delete). 복식부기 데이터는 유지됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "거래를 찾을 수 없습니다"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteTransaction(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        log.info("DELETE /api/v1/transactions/{} - userId={}", id, userId);
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.ok(ResponseFactory.successWithMessage("거래 삭제 완료"));
    }
}