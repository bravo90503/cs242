package edu.ucr.cs242.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.ucr.cs242.repo.model.Keyword;

public interface KeywordRepository extends MongoRepository<Keyword, String> {

}