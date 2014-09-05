package com.example.clientapp;

import java.security.KeyFactory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import android.util.Base64;

public class encryptionRSA {

	private final static String RSA = "RSA";

	// RSA 1024 Keypair -> PublicKey und PrivatKey generieren
	public static KeyPair generateKey() throws Exception {
		KeyPairGenerator gen = KeyPairGenerator.getInstance(RSA);
		gen.initialize(1024);
		KeyPair keyPair = gen.generateKeyPair();
		return keyPair;
	}

	private static byte[] encrypt(PublicKey pubRSA, String text)
			throws Exception {
		Cipher cipher = Cipher.getInstance(RSA);
		cipher.init(Cipher.ENCRYPT_MODE, pubRSA);
		return cipher.doFinal(text.getBytes());
	}

	// Übergebenen String mit einem RSA PubliyKey verschlüsseln
	// -> in der App AES Key mit Publickey verschlüsseln
	public final static String encrypt(String text, PublicKey pubkey) {
		try {
			return byte2hex(encrypt(pubkey, text));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// Übergebenen String mit einem RSA PrivateKey entschlüsseln
	// -> in der App AES Key mit Privatekey entschlüsseln
	public final static String decrypt(String data, PrivateKey prkey) {
		try {
			return new String(decrypt(hex2byte(data.getBytes()), prkey));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] decrypt(byte[] src, PrivateKey prkey)
			throws Exception {
		Cipher cipher = Cipher.getInstance(RSA);
		cipher.init(Cipher.DECRYPT_MODE, prkey);
		return cipher.doFinal(src);
	}

	// Funktion zum umwandeln von Byte Arrays in einen String
	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1)
				hs += ("0" + stmp);
			else
				hs += stmp;
		}
		return hs.toUpperCase();
	}

	// Funktion zum umwandeln von Strings in eine byte Array
	public static byte[] hex2byte(byte[] b) {
		if ((b.length % 2) != 0)
			throw new IllegalArgumentException("hello");

		byte[] b2 = new byte[b.length / 2];

		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	// Funktion zum umwandeln von einem PublicKey in einen String
	public static String publicKeyToString(PublicKey p) {
		byte[] publicKeyBytes = p.getEncoded();
		String value = Base64.encodeToString(publicKeyBytes, Base64.DEFAULT);
		return value;
	}

	// Funktion zum umwandeln von einem String in einen PublicKey
	public static PublicKey stringToPublicKey(String s) {
		byte[] c = null;
		KeyFactory keyFact = null;
		PublicKey returnKey = null;
		try {
			c = Base64.decode(s, Base64.DEFAULT);
			keyFact = KeyFactory.getInstance(RSA);
		} catch (Exception e) {
			System.out.println("Error in Keygen");
			e.printStackTrace();
		}
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(c);
		try {
			returnKey = keyFact.generatePublic(x509KeySpec);
		} catch (Exception e) {
			System.out.println("Error in Keygen2");
			e.printStackTrace();
		}
		return returnKey;
	}
}
