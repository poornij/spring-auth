package com.devstaq.auth.persistence.repository;

import com.devstaq.auth.persistence.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface RoleRepository.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

	/**
	 * Find by name.
	 *
	 * @param name the name
	 * @return the role
	 */
	Role findByName(String name);

	/**
	 * Delete.
	 *
	 * @param role the role
	 */
	@Override
	void delete(Role role);
}
