package com.revature.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.revature.exceptions.UserNotFoundException;
import com.revature.models.Cart;
import com.revature.models.Item;
import com.revature.models.Role;
import com.revature.models.User;
import com.revature.repositories.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private HttpServletRequest req;

	@Autowired
	private UserRepository userDAO;
	
	public List<User> findAll() {
		return userDAO.findAll();
	}
	
	public User findById(int id) {
		return userDAO.findById(id)
				.orElseThrow(() -> new UserNotFoundException(String.format("No user with id = %d", id)));
	}
	
	public User insert(User user) {
		if (user.getId() != 0) {
			// This should be a custom exception class instead
			throw new RuntimeException("User ID must be zero to create a new User");
		}

		user.setCart(new Cart());
		user.setRole(Role.CUSTOMER);
		user.getCart().setItemsList(new ArrayList<Item>());
		
		userDAO.save(user); // Modify the user with the new ID
		
		return user;
	}
	
	public User update(User user) {
		if(!userDAO.existsById(user.getId())) {
			throw new RuntimeException("User must already exist to update");
		}
		
		userDAO.save(user);
		
		HttpSession session = req.getSession(false); // They must have already been logged in, because we had our guard method
		
		User sessionUser = (User) session.getAttribute("currentUser");
		
		// If a User updated themselves, update the information in the session
		if(sessionUser.getId() == user.getId()) {
			session.setAttribute("currentUser", user);
		}
		
		return user;
	}
	
	public boolean delete(int id) {
		if(!userDAO.existsById(id)) {
			return false;
		}
		
		userDAO.deleteById(id);
		
		return true;
	}

	public User login(String username, String password) {
		User exists = userDAO.findByUsernameAndPassword(username, password)
							.orElseThrow(() -> new UserNotFoundException(
								String.format("No User with username = %s or wrong password", username)));
		// Check that the given password matches the password in the User object
		// Pretend that they were successful
		
		
		HttpSession session = req.getSession();
		session.setAttribute("currentUser", exists);
		
		return exists;
	}
	
	public void logout() {
		
		HttpSession session = req.getSession(false);
		
		if(session == null) {
			// No one was logged in
			
			return;
		}
		
		session.invalidate();
	}
}
