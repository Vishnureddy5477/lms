package com.cranesvarsity.template.dto;

import java.util.List;

public record DlSubtopicNode(int subtopicId, String subtopicName, List<DlVideoNode> videos) {
}
