package com.personalaccount.domain.transaction.dto.mapper;

import com.personalaccount.domain.transaction.dto.response.TransactionDetailResponse;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import com.personalaccount.domain.transaction.entity.JournalEntry;
import com.personalaccount.domain.transaction.entity.Transaction;
import com.personalaccount.domain.transaction.entity.TransactionDetail;

import java.util.List;

public class TransactionMapper {

    public static TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .bookId(transaction.getBook().getId())
                .date(transaction.getDate())
                .type(transaction.getType())
                .memo(transaction.getMemo())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    public static TransactionDetailResponse toDetailResponse(
            Transaction transaction,
            List<JournalEntry> journalEntries,
            List<List<TransactionDetail>> detailsList
    ) {
        if (transaction == null) {
            return null;
        }

        List<TransactionDetailResponse.JournalEntryInfo> journalEntryInfos =
                journalEntries.stream()
                        .map(je -> {
                            int index = journalEntries.indexOf(je);
                            List<TransactionDetail> details = detailsList.get(index);

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
                .memo(transaction.getMemo())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .journalEntries(journalEntryInfos)
                .build();
    }
}