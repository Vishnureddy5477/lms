package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.DigitalContentDao;
import com.cranesvarsity.template.dto.*;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.util.YouTubeLinkParser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Backs the Digital Content page — successor of legacy view-content.jsp /
 * get-content.jsp. Course domain maps to a hardcoded category token, exactly
 * as in legacy; the cascading Module -> Topic -> Subtopic -> Video AJAX calls
 * are consolidated into one nested response per module (same hierarchy/data,
 * just fewer round trips).
 */
@Service
public class DigitalContentService {

    private final AdmissionRepository admissionRepository;
    private final DigitalContentDao digitalContentDao;

    public DigitalContentService(AdmissionRepository admissionRepository, DigitalContentDao digitalContentDao) {
        this.admissionRepository = admissionRepository;
        this.digitalContentDao = digitalContentDao;
    }

    public List<DlModuleItem> listModules(AuthenticatedStudent student) {
        Admission admission = admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));

        String token = tokenForDomain(admission.getCourseDomain());
        return digitalContentDao.findModulesByToken(token).stream()
                .map(row -> new DlModuleItem(row.moduleId(), row.moduleName(), row.seqNo()))
                .toList();
    }

    public DlModuleContent getModuleContent(int moduleId) {
        DigitalContentDao.Counts counts = digitalContentDao.findCounts(moduleId);

        List<DlTopicNode> topics = digitalContentDao.findTopics(moduleId).stream()
                .map(topic -> {
                    List<DlSubtopicNode> subtopics = digitalContentDao.findSubtopics(topic.topicId()).stream()
                            .map(subtopic -> {
                                List<DlVideoNode> videos = digitalContentDao.findVideos(subtopic.subtopicId()).stream()
                                        .map(video -> new DlVideoNode(video.id(), video.name(), video.link(), YouTubeLinkParser.extractVideoId(video.link())))
                                        .toList();
                                return new DlSubtopicNode(subtopic.subtopicId(), subtopic.subtopicName(), videos);
                            })
                            .toList();
                    return new DlTopicNode(topic.topicId(), topic.topicName(), subtopics);
                })
                .toList();

        return new DlModuleContent(counts.totalTopics(), counts.totalSubtopics(), counts.totalVideos(), topics);
    }

    /** Copied verbatim from view-content.jsp's hardcoded course-domain -> token mapping. */
    private String tokenForDomain(String courseDomain) {
        if (courseDomain == null) {
            return "other";
        }
        if (courseDomain.equalsIgnoreCase("embedded")) {
            return "EMB7X9PQL83D2RT56";
        }
        if (courseDomain.equalsIgnoreCase("vlsi")) {
            return "VLSI7X9PQL83D2RT56";
        }
        if (courseDomain.equalsIgnoreCase("data science")) {
            return "DS7X9PQL83D2RT56";
        }
        return "other";
    }
}
