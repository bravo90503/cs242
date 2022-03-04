package edu.ucr.cs242.repo;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import edu.ucr.cs242.repo.model.Keyword;

@Profile("mongo")
public interface KeywordRepository extends MongoRepository<Keyword, String> {

	@Query("{_id: { $in: ?0 } })")
	List<Keyword> findByIds(List<String> ids);

}