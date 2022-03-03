package edu.ucr.cs242.repo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import edu.ucr.cs242.repo.model.Keyword;

@Profile("mongo")
public interface KeywordRepository extends MongoRepository<Keyword, String> {

}