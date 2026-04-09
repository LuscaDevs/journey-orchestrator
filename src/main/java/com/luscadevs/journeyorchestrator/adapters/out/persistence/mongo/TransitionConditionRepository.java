package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo;

import com.luscadevs.journeyorchestrator.domain.journey.TransitionCondition;
import com.luscadevs.journeyorchestrator.application.port.TransitionConditionRepositoryPort;

import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB implementation for transition condition repository.
 * 
 * This adapter provides MongoDB persistence for transition conditions,
 * following hexagonal architecture principles.
 */
@Component
public class TransitionConditionRepository implements TransitionConditionRepositoryPort {
    
    private static final String COLLECTION_NAME = "transitionConditions";
    
    private final MongoTemplate mongoTemplate;
    
    public TransitionConditionRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public TransitionCondition save(TransitionCondition condition) {
        mongoTemplate.save(condition, COLLECTION_NAME);
        return condition;
    }
    
    @Override
    public Optional<TransitionCondition> findById(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        
        TransitionCondition condition = mongoTemplate.findOne(query, TransitionCondition.class, COLLECTION_NAME);
        return Optional.ofNullable(condition);
    }
    
    @Override
    public List<TransitionCondition> findByJourneyDefinitionId(String journeyDefinitionId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("journeyDefinitionId").is(journeyDefinitionId));
        
        return mongoTemplate.find(query, TransitionCondition.class, COLLECTION_NAME);
    }
    
    @Override
    public boolean deleteById(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        
        TransitionCondition deleted = mongoTemplate.findAndRemove(query, TransitionCondition.class, COLLECTION_NAME);
        return deleted != null;
    }
    
    @Override
    public List<TransitionCondition> findByTransitionId(String transitionId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("transitionId").is(transitionId));
        
        return mongoTemplate.find(query, TransitionCondition.class, COLLECTION_NAME);
    }
}
