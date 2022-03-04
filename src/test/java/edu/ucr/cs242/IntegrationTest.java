package edu.ucr.cs242;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucr.cs242.repo.KeywordRepository;
import edu.ucr.cs242.repo.UserRepository;
import edu.ucr.cs242.repo.model.Document;
import edu.ucr.cs242.repo.model.Keyword;
import edu.ucr.cs242.repo.model.User;

@RunWith(SpringRunner.class)

//@SpringBootTest(classes = { Application.class }, webEnvironment =
//WebEnvironment.DEFINED_PORT)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@TestMethodOrder(OrderAnnotation.class)
@ActiveProfiles("mongo")
public class IntegrationTest {
	private static ObjectMapper mapper = new ObjectMapper();
	@Autowired
	UserRepository userRepo;
	@Autowired
	KeywordRepository keywordRepo;

	@Test
	public void testmsg() {

		System.out.println("hello");

	}

	@Test
	public void findUser() {

		Optional<User> user = userRepo.findById("cynthia");

		Assert.assertTrue(user.isPresent());
		Assert.assertTrue(user.get().getId().equals("cynthia"));

	}

	@Test
	@Order(1)
	public void addUser() {

		User user = new User();
		user.setId("luis");
		user.setPassword("password");

		userRepo.save(user);

	}

	@Test
	@Order(2)
	public void updateUser() {

		User user = new User();
		user.setId("luis");
		user.setPassword("password2");

		// sees luis is already in db and updates rather than insert a new record
		userRepo.save(user);

	}

	@Test
	@Order(3)
	public void updateUserAnotherWay() {

		Optional<User> user = userRepo.findById("luis");
		Assert.assertTrue(user.isPresent());
		User luis = user.get();
		luis.setPassword("password3");

		userRepo.save(luis);
	}

	@Test
	@Order(4)
	public void deleteUser() throws InterruptedException {
		Optional<User> user = userRepo.findById("luis");

		Assert.assertTrue(user.isPresent());
		User luis = user.get();

		userRepo.delete(luis);

		Optional<User> findUser = userRepo.findById("luis");

		// assert luis no longer in db
		Assert.assertFalse(findUser.isPresent());

	}

	public void KeywordToMongo() {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
		try {
			FileInputStream stream = new FileInputStream(resource.getPath() + "part-r-00000.gz"); // creates a new file
			// instance
			GZIPInputStream gzipstream = new GZIPInputStream(stream);
			Reader decoder = new InputStreamReader(gzipstream, "UTF-8");
			BufferedReader buffered = new BufferedReader(decoder);
			StringBuffer keysb = new StringBuffer(); // constructs a string buffer with no characters
			StringBuffer documents = new StringBuffer(); // constructs a string buffer with no characters
			String line;
			while ((line = buffered.readLine()) != null) {

				int beginIndex = line.indexOf("{");
				String key = line.substring(0, beginIndex).trim();
				if (StringUtils.isNotBlank(key) && key.equals("information")) {
					keysb.append(key); // appends line to string buffer
					documents.append(line.substring(beginIndex, line.length()));

					break;
				}
			}
			buffered.close(); // closes the stream and release the resources
			System.out.println("Json Contents of File: ");
			System.out.println(keysb.toString()); // returns a string that textually represents the object
			System.out.println(documents.toString()); // returns a string that textually represents the object

			Keyword keyword = mapper.readValue(documents.toString(), Keyword.class);
			keyword.setKey(keysb.toString());
			keywordRepo.save(keyword);
			Double maxScore = null;
			Assert.assertTrue(String.format("expected 1001, got %d", keyword.getDocuments().size()),
					keyword.getDocuments().size() == 1001);
			for (Document document : keyword.getDocuments()) {
				System.out.println("docId=" + document.getDocId() + ", score=" + document.getScore());
				if (maxScore == null) {
					maxScore = new Double(document.getScore());
					// continue;
				}

				Assert.assertTrue(maxScore >= document.getScore());
			}

		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}

}
