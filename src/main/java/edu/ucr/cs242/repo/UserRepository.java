package edu.ucr.cs242.repo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import edu.ucr.cs242.repo.model.User;

@Profile("mongo")
public interface UserRepository extends MongoRepository<User, String> {

}