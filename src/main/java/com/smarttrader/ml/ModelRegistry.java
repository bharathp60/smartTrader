package com.smarttrader.ml;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModelRegistry {

    private final List<TradingModel> models;

    public ModelRegistry(List<TradingModel> models) {
        this.models = models;
    }

    public TradingModel getDeployedModel() {
        return models.stream()
                .filter(TradingModel::isDeployed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No deployed model found"));
    }

    public void registerModel(TradingModel model) {
        if (!models.contains(model)) {
            models.add(model);
        }
    }

    public Optional<TradingModel> getModelByName(String name) {
        return models.stream()
                .filter(m -> m.getModelName().equals(name))
                .findFirst();
    }

    public List<TradingModel> listModels() {
        return models;
    }
}
