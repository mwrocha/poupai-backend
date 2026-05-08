package io.poupai.backend.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class TagDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagResponse {
        private String id;       // slug da categoria (ex: "alimentacao")
        private String name;     // nome original (ex: "Alimentação")
        private Double totalSpent;
        private Long transactionCount;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagsSummaryResponse {
        private List<TagResponse> tags;
        private Double totalSpent;
        private Integer month;
        private Integer year;
    }
}