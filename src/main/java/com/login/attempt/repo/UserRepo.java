package com.login.attempt.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.login.attempt.entity.*;

public interface UserRepo extends JpaRepository<User, Integer> {

	public User findByEmail(String emaill);

	public User findByVerificationCode(String code);

	@Query("update User u set u.failedAttempt=?1 where email=?2 ")
	@Modifying
	public void updateFailedAttempt(int attempt, String email);

}
