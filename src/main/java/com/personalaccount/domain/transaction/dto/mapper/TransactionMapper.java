package com.personalaccount.domain.transaction.dto.mapper;

import com.personalaccount.domain.transaction.dto.response.TransactionDetailResponse;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import com.personalaccount.domain.transaction.entity.JournalEntry;
import com.personalaccount.domain.transaction.entity.Transaction;
import com.personalaccount.domain.transaction.entity.TransactionDetail;

import java.util.List;

/**
 * Transaction Entity ↔ DTO 변환 Mapper
 * - static 메서드로 변환 제공
 */
public class TransactionMapper {

    /**
     * Entity → Response DTO 변환
     * - 단순 거래 정보만 (복식부기 제외)
     */
    public static TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .bookId(transaction.getBook().getId())
                .date(transaction.getDate())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .memo(transaction.getMemo())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    /**
     * Entity → DetailResponse DTO 변환
     * - 복식부기 상세 포함 (JournalEntry, TransactionDetail)
     *
     * @param transaction 거래 Entity
     * @param journalEntries 분개 목록
     * @param detailsList 차변/대변 상세 목록 (분개별)
     */
    public static TransactionDetailResponse toDetailResponse(
            Transaction transaction,
            List<JournalEntry> journalEntries,
            List<List<TransactionDetail>> detailsList
    ) {
        if (transaction == null) {
            return null;
        }

        // 분개 정보 변환
        List<TransactionDetailResponse.JournalEntryInfo> journalEntryInfos =
                journalEntries.stream()
                        .map(je -> {
                            int index = journalEntries.indexOf(je);
                            List<TransactionDetail> details = detailsList.get(index);

                            // 차변/대변 상세 변환
                            List<TransactionDetailResponse.DetailInfo> detailInfos =
                                    details.stream()
                                            .map(detail -> TransactionDetailResponse.DetailInfo.builder()
                                                    .id(detail.getId())
                                                    .accountCode(detail.getAccount().getCode())
                                                    .accountName(detail.getAccount().getName())
                                                    .detailType(detail.getDetailType().name())
                                                    .debitAmount(detail.getDebitAmount())
                                                    .creditAmount(detail.getCreditAmount())
                                                    .build())
                                            .toList();

                            return TransactionDetailResponse.JournalEntryInfo.builder()
                                    .id(je.getId())
                                    .description(je.getDescription())
                                    .details(detailInfos)
                                    .build();
                        })
                        .toList();

        return TransactionDetailResponse.builder()
                .id(transaction.getId())
                .bookId(transaction.getBook().getId())
                .date(transaction.getDate())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .memo(transaction.getMemo())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .journalEntries(journalEntryInfos)
                .build();
    }
}