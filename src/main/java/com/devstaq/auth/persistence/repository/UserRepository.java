package com.devstaq.auth.persistence.repository;

import com.devstaq.auth.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface UserRepository.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Find by email.
	 *
	 * @param email the email
	 * @return the user
	 */
	User findByEmail(String email);

	/**
	 * Delete.
	 *
	 * @param user the user
	 */
	@Override
	void delete(User user);
}
