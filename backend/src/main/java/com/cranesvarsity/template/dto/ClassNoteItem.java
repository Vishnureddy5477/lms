package com.cranesvarsity.template.dto;

public record ClassNoteItem(
        int slNo,
        String postedDate,
        String postedBy,
        String batch,
        String module,
        String attachFile,
        String content
) {
}
