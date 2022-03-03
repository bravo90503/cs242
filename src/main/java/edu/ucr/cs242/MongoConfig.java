package edu.ucr.cs242;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@Profile("mongo")
public class MongoConfig {
	@Bean
	public MongoClient mongoClient() {
		ConnectionString connectionString = new ConnectionString(
				"mongodb+srv://edu-ucr-cs242:edu-ucr-cs242@cluster0.qvv7z.mongodb.net/test?retryWrites=true&w=majority");
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongoClient(), "test");
	}

}
