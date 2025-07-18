package com.devstaq.auth.persistence.repository;

import com.devstaq.auth.persistence.model.User;
import com.devstaq.auth.persistence.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.stream.Stream;

/**
 * The Interface VerificationTokenRepository.
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

	/**
	 * Find by token.
	 *
	 * @param token the token
	 * @return the verification token
	 */
	VerificationToken findByToken(String token);

	/**
	 * Find by user.
	 *
	 * @param user the user
	 * @return the verification token
	 */
	VerificationToken findByUser(User user);

	/**
	 * Find all by expiry date less than.
	 *
	 * @param now the now
	 * @return the stream
	 */
	Stream<VerificationToken> findAllByExpiryDateLessThan(Date now);

	/**
	 * Delete by expiry date less than.
	 *
	 * @param now the now
	 */
	void deleteByExpiryDateLessThan(Date now);

	/**
	 * Delete all expired since.
	 *
	 * @param now the now
	 */
	@Modifying
	@Query("delete from VerificationToken t where t.expiryDate <= ?1")
	void deleteAllExpiredSince(Date now);
}
