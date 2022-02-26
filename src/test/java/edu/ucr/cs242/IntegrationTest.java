package edu.ucr.cs242;

import java.util.Optional;

import org.junit.Assert;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
@TestMethodOrder(OrderAnnotation.class)
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
		// let's make sure our last update order(3) actually worked
		Assert.assertTrue(luis.getPassword().equals("password3"));
		
		userRepo.delete(luis);
		
		
		Optional<User> findUser = userRepo.findById("luis");

		
		// assert luis no longer in db
		Assert.assertFalse(findUser.isPresent());

	}

}

