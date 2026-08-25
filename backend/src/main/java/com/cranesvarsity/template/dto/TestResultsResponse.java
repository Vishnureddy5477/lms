package com.cranesvarsity.template.dto;

import java.util.List;

public record TestResultsResponse(List<TestResultRow> results, TestResultsSummary summary) {
}
