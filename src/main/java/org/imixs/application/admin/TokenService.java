package org.imixs.application.admin;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.inject.Singleton;

import org.imixs.jwt.HMAC;
import org.imixs.jwt.JWTBuilder;
import org.imixs.jwt.JWTException;
import org.imixs.jwt.JWTParser;
import org.imixs.workflow.exceptions.AccessDeniedException;

/**
 * The TokenService can be used to generate and validate Json Web Token (JWT).
 * The tokens are used by the rest api to grant access
 * 
 * @author rsoika
 */
@Singleton
public class TokenService {

	private byte[] secret = null;

	/**
	 * Generates the encryption secret.
	 * 
	 * @throws AccessDeniedException
	 */
	@PostConstruct
	public void startup() {
		secret = UUID.randomUUID().toString().getBytes();
	}

	public String generateAccessToken(String api, String userid, String password, String autMethod)
			throws JWTException {

		String token = null;
		// add the a signing key...
		SecretKey secretKey = HMAC.createKey("HmacSHA256", secret);
		JWTBuilder builder = new JWTBuilder().setKey(secretKey).setClaim("api", api).setClaim("sub", userid)
				.setClaim("secret", password).setClaim("autmethod", autMethod);

		token = builder.build().getToken();

		return token;
	}

	public String getPayload(String token) throws JWTException {

		SecretKey secretKey = HMAC.createKey("HmacSHA256", secret);

		// verify token and get the payload...
		String payload = new JWTParser().setKey(secretKey).setToken(token).verify().getPayload();
		// payload will result in:
		// {"sub":"1234567890","name":"John Doe","admin":true}
		return payload;

	}
}