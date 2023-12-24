package com.login.attempt.service;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.login.attempt.entity.*;
import com.login.attempt.repo.*;
import com.login.attempt.service.*;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private JavaMailSender mailSender;

	@Override
	public User saveUser(User user, String url) {

		String password = passwordEncoder.encode(user.getPassword());
		user.setPassword(password);
		user.setRole("ROLE_USER");

		user.setEnable(false);
		user.setVerificationCode(UUID.randomUUID().toString());

		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		user.setLockTime(null);
		
		User newuser = userRepo.save(user);

		if (newuser != null) {
			sendEmail(newuser, url);
		}

		return newuser;
	}

	@Override
	public void sendEmail(User user, String url) {

		String from = "daspabitra55@gmail.com";
		String to = user.getEmail();
		String subject = "Account Verfication";
		String content = "Dear [[name]],<br>" + "Please click the link below to verify your registration:<br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" + "Thank you,<br>" + "Becoder";

		try {

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);

			helper.setFrom(from, "Becoder");
			helper.setTo(to);
			helper.setSubject(subject);

			content = content.replace("[[name]]", user.getName());
			String siteUrl = url + "/verify?code=" + user.getVerificationCode();

			System.out.println(siteUrl);

			content = content.replace("[[URL]]", siteUrl);

			helper.setText(content, true);

			mailSender.send(message);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean verifyAccount(String verificationCode) {

		User user = userRepo.findByVerificationCode(verificationCode);

		if (user == null) {
			return false;
		} else {

			user.setEnable(true);
			user.setVerificationCode(null);

			userRepo.save(user);

			return true;
		}

	}

	@Override
	public void removeSessionMessage() {

		HttpSession session = ((ServletRequestAttributes) (RequestContextHolder.getRequestAttributes())).getRequest()
				.getSession();

		session.removeAttribute("msg");
	}

	@Override
	public void increaseFailedAttempt(User user) {

		int attempt = user.getFailedAttempt() + 1;

		userRepo.updateFailedAttempt(attempt, user.getEmail());

	}

	 private static final long lock_duration_time=24*60*60*1000;
	//private static final long lock_duration_time = 30000;

	public static final long ATTEMPT_TIME = 3;

	@Override
	public void resetAttempt(String email) {
		userRepo.updateFailedAttempt(0, email);
	}

	@Override
	public void lock(User user) {
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepo.save(user);
	}

	@Override
	public boolean unlockAccountTimeExpired(User user) {

		long lockTimeInMills = user.getLockTime().getTime();
		long currentTimeMillis = System.currentTimeMillis();

		if (lockTimeInMills + lock_duration_time < currentTimeMillis) {
			user.setAccountNonLocked(true);
			user.setLockTime(null);
			user.setFailedAttempt(0);
			userRepo.save(user);
			return true;
		}
		return false;
	}

}
