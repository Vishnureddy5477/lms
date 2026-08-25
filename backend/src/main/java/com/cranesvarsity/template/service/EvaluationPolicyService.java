package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.EvaluationCriteriaDao;
import com.cranesvarsity.template.dto.EvaluationPolicyResponse;
import org.springframework.stereotype.Service;

/** Backs the Evaluation Policy page — successor of legacy evaluation-criterias.jsp (module + placement tabs only). */
@Service
public class EvaluationPolicyService {

    private final EvaluationCriteriaDao dao;

    public EvaluationPolicyService(EvaluationCriteriaDao dao) {
        this.dao = dao;
    }

    public EvaluationPolicyResponse getPolicy() {
        return new EvaluationPolicyResponse(dao.findModuleCriteria(), dao.findPlacementCriteria());
    }
}
