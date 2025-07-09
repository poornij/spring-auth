package com.devstaq.auth.persistence.repository;

import com.devstaq.auth.persistence.model.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface PrivilegeRepository.
 */
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

	/**
	 * Find by name.
	 *
	 * @param name the name
	 * @return the privilege
	 */
	Privilege findByName(String name);

	/**
	 * Delete.
	 *
	 * @param privilege the privilege
	 */
	@Override
	void delete(Privilege privilege);
}
