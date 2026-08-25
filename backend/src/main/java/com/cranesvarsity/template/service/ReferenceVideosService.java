package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.ReferenceVideoDao;
import com.cranesvarsity.template.dto.ReferenceVideoItem;
import com.cranesvarsity.template.util.YouTubeLinkParser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Backs the Reference Videos page — successor of legacy
 * reference-video-modules.jsp / manage-reference-videos.jsp. The module list
 * is intentionally unfiltered by course/batch, matching legacy exactly (every
 * student sees every module regardless of enrollment).
 */
@Service
public class ReferenceVideosService {

    private final ReferenceVideoDao referenceVideoDao;

    public ReferenceVideosService(ReferenceVideoDao referenceVideoDao) {
        this.referenceVideoDao = referenceVideoDao;
    }

    public List<String> listModules() {
        return referenceVideoDao.findDistinctModules();
    }

    public List<ReferenceVideoItem> listVideos(String moduleName) {
        return referenceVideoDao.findByModule(moduleName).stream()
                .map(row -> new ReferenceVideoItem(
                        row.id(), row.title(), row.link(), YouTubeLinkParser.extractVideoId(row.link()), row.duration()
                ))
                .toList();
    }
}
