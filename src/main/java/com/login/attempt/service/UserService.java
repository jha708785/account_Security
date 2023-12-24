package com.login.attempt.service;

import com.login.attempt.entity.*;
import com.login.attempt.repo.*;
import com.login.attempt.service.*;

public interface UserService {

	public User saveUser(User user, String url);

	public void removeSessionMessage();

	public void sendEmail(User user, String path);

	public boolean verifyAccount(String verificationCode);

	public void increaseFailedAttempt(User user);

	public void resetAttempt(String email);

	public void lock(User user);

	public boolean unlockAccountTimeExpired(User user);

}
