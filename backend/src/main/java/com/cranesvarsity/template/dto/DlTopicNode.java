package com.cranesvarsity.template.dto;

import java.util.List;

public record DlTopicNode(int topicId, String topicName, List<DlSubtopicNode> subtopics) {
}
