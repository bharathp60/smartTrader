package com.smarttrader.learning;

import com.smarttrader.entity.AdaptationProposal;
import com.smarttrader.entity.AdaptationStatus;
import com.smarttrader.repository.AdaptationProposalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdaptationEngine {

    private final AdaptationProposalRepository repository;

    public AdaptationEngine(AdaptationProposalRepository repository) {
        this.repository = repository;
    }

    public boolean evaluateProposal(AdaptationProposal proposal) {
        return proposal.getSampleSize() != null && proposal.getSampleSize() >= 10;
    }

    public void deployProposal(AdaptationProposal proposal) {
        proposal.setStatus(AdaptationStatus.DEPLOYED);
        repository.save(proposal);
    }

    public void rollbackIfDegraded(double currentPerformance, double previousPerformance, double threshold) {
        if (currentPerformance < previousPerformance * (1 - threshold)) {
            List<AdaptationProposal> deployed = repository.findByStatus(AdaptationStatus.DEPLOYED.name());
            if (!deployed.isEmpty()) {
                AdaptationProposal last = deployed.get(0);
                last.setStatus(AdaptationStatus.ROLLED_BACK);
                repository.save(last);
            }
        }
    }

    public void checkForAutoRollback() {
        // Scheduled task implementation
    }
}
