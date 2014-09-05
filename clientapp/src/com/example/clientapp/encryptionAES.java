package com.example.clientapp;

import java.security.*;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import android.util.*;

public class encryptionAES {

	// AES als Algorithmus wählen
	private static final String ALGORITHM = "AES";

	// Verschlüsseln eines Strings mit einem AES Key
	public static String encrypt(String valueToEnc, byte[] keyValue)
			throws Exception {
		// AES Key aus dem Keyvalue generieren
		Key key = generateKey(keyValue);
		Cipher c = Cipher.getInstance(ALGORITHM);
		// Verschlüsseln eines Strings mit einem AES Key
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encValue = c.doFinal(valueToEnc.getBytes());
		// mit Base64 in String codieren
		String encryptedValue = Base64.encodeToString(encValue, Base64.DEFAULT);
		return encryptedValue;
	}

	// Entschlüsseln eines Strings mit einem AES Key
	public static String decrypt(String encryptedValue, byte[] keyValue)
			throws Exception {
		// AES Key aus dem Keyvalue generieren
		Key key = generateKey(keyValue);
		Cipher c = Cipher.getInstance(ALGORITHM);
		// Entschlüsseln eines Strings mit einem AES Key
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] decordedValue = Base64.decode(encryptedValue, Base64.DEFAULT);
		byte[] decValue = c.doFinal(decordedValue);
		// mit Base64 in String codieren
		String decryptedValue = new String(decValue);
		return decryptedValue;
	}

	private static Key generateKey(byte[] keyValue) throws Exception {
		Key key = new SecretKeySpec(keyValue, ALGORITHM);
		return key;
	}

	// Random AES Key aus den angegeben Zeichen generieren und als String
	// zurückgeben
	public static String RandomkeyValue(int len) {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		Random rnd = new Random();

		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}
}