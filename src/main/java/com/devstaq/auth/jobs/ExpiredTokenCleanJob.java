package com.devstaq.auth.jobs;

import com.devstaq.auth.persistence.repository.PasswordResetTokenRepository;
import com.devstaq.auth.persistence.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

/**
 * The ExpiredTokenCleanJob is a Service which purges expired registration email verification tokens and password reset tokens based on the schedule
 * defined in user.purgetokens.cron.expression in your application.properties.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExpiredTokenCleanJob {

	/** The registration email verification token repository. */
	private final VerificationTokenRepository verificationTokenRepository;

	/** The password reset token repository. */
	private final PasswordResetTokenRepository passwordTokenRepository;

	/**
	 * Purge expired.
	 */
	@Scheduled(cron = "${user.purgetokens.cron.expression}")
	public void purgeExpired() {
		log.info("ExpiredTokenCleanJob.purgeExpired: running....");
		Date now = Date.from(Instant.now());

		passwordTokenRepository.deleteAllExpiredSince(now);
		verificationTokenRepository.deleteAllExpiredSince(now);
		log.info("ExpiredTokenCleanJob.purgeExpired: all expired tokens have been deleted.");
	}
}
