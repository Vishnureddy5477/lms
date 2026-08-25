package com.cranesvarsity.template.dto;

/** One row of the Course Outline / Lab Manual page. hasContent=false → "No outline available" badge. */
public record CourseModuleItem(String moduleName, String contentUrl, boolean hasContent) {
}
