package org.imixs.application.admin;

import javax.crypto.SecretKey;

import org.imixs.jwt.HMAC;
import org.imixs.jwt.JWTBuilder;
import org.imixs.jwt.JWTException;

/**
 * This test generates a Json Web Token (JWT) to be used for access
 * 
 * 
 * @author rsoika
 */
public class JWTGenerator {

	public static String generateAccessToken(String api, String userid, String secret, String autMethod) {

		String token = null;
		// We need a signing key...
		SecretKey secretKey = HMAC.createKey("HmacSHA256", "secret".getBytes());

		String payload = "{\"sub\":\"" + userid + "\",\"api\":\"" + api + "\",\"secret\":\"" + secret
				+ "\",\"autmethod\":[\"" + autMethod + "\"]}";

		System.out.println("Payload=" + payload);
		JWTBuilder builder = new JWTBuilder().setKey(secretKey).setPayload(payload);
		try {
			token = builder.getToken();

			System.out.println("JWT=" + token);
		} catch (JWTException e) {
			e.printStackTrace();

		}

		return token;
	}

}