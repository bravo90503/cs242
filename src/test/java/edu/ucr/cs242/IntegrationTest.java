package edu.ucr.cs242;

import java.util.Optional;

import org.junit.Assert;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import edu.ucr.cs242.repo.UserRepository;
import edu.ucr.cs242.repo.model.User;


@RunWith(SpringRunner.class)

//@SpringBootTest(classes = { Application.class }, webEnvironment =
//WebEnvironment.DEFINED_PORT)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class IntegrationTest {
	@Autowired
	UserRepository userRepo;

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
		
		userRepo.save(user);

	}
	
    @Test
	@Order(3)
	public void deleteUser() {
		
		Optional<User> user = userRepo.findById("luis");

		Assert.assertTrue(user.isPresent());
		User luis = user.get();
		
		
		userRepo.delete(luis);
		
		
		Optional<User> findUser = userRepo.findById("luis");
		// assert luis no longer in db
		Assert.assertFalse(findUser.isPresent());

	}

}

