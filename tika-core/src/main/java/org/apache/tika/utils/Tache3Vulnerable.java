package org.apache.tika.utils;

import java.security.MessageDigest;

import javax.crypto.Cipher;

/**
 * Classe volontairement vulnerable (tache 3) : sert uniquement a verifier
 * que le workflow Semgrep echoue quand une nouvelle vulnerabilite est ajoutee.
 * A supprimer dans le commit suivant.
 */
public class Tache3Vulnerable {

    public static byte[] hash(byte[] data) throws Exception {
        // MD5 : algorithme de hachage casse
        return MessageDigest.getInstance("MD5").digest(data);
    }

    public static byte[] encrypt(byte[] data, javax.crypto.SecretKey key) throws Exception {
        // DES : chiffrement obsolete
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}
