package com.cranesvarsity.template.dto;

import java.util.List;

public record DlModuleContent(
        int totalTopics,
        int totalSubtopics,
        int totalVideos,
        List<DlTopicNode> topics
) {
}
