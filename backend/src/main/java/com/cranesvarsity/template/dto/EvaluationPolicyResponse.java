package com.cranesvarsity.template.dto;

import java.util.List;

public record EvaluationPolicyResponse(List<ModuleEvaluationRow> moduleCriteria, List<PlacementEvaluationRow> placementCriteria) {
}
