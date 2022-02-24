package edu.ucr.cs242.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.ucr.cs242.repo.model.User;

public interface UserRepository extends MongoRepository<User, String> {

}