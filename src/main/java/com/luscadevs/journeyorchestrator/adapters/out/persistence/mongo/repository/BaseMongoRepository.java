package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * Base repository class for MongoDB operations.
 * Provides common CRUD operations and pagination support.
 */
public abstract class BaseMongoRepository<T, ID> {

    protected final MongoRepository<T, ID> mongoRepository;
    protected final MongoTemplate mongoTemplate;

    public BaseMongoRepository(MongoRepository<T, ID> mongoRepository, MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Save entity.
     */
    public T save(T entity) {
        return mongoRepository.save(entity);
    }

    /**
     * Find entity by ID.
     */
    public java.util.Optional<T> findById(ID id) {
        return mongoRepository.findById(id);
    }

    /**
     * Find all entities.
     */
    public List<T> findAll() {
        return mongoRepository.findAll();
    }

    /**
     * Delete entity by ID.
     */
    public void deleteById(ID id) {
        mongoRepository.deleteById(id);
    }

    /**
     * Check if entity exists by ID.
     */
    public boolean existsById(ID id) {
        return mongoRepository.existsById(id);
    }

    /**
     * Find entities with pagination.
     */
    public Page<T> findAll(Pageable pageable) {
        List<T> content = mongoRepository.findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), content.size());
        List<T> pageContent = content.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, content.size());
    }

    /**
     * Count all entities.
     */
    public long count() {
        return mongoRepository.count();
    }

    /**
     * Custom query execution.
     */
    protected List<T> find(Query query) {
        return mongoTemplate.find(query, getEntityClass());
    }

    /**
     * Get entity class for type safety.
     */
    protected abstract Class<T> getEntityClass();
}
