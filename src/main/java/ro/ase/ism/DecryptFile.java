/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ase.ism;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author costin1989
 */
public class DecryptFile {
    
    public DecryptFile(){
        
    }

    public static String getDecryptFile(String encrypted, String password) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(),"salt".getBytes(), 1024, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey skeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

            // build the initialization vector.  This example is all zeros, but it 
            // could be any value or generated using a random number generator.
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            // initialize the cipher for encrypt mode
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            
            // reinitialize the cipher for decryption
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

            // decrypt the message
            byte[] d = Base64.decodeBase64(encrypted);
            byte[] decrypted = cipher.doFinal(d);
            String result = new String(decrypted);
            System.out.println("Decrypted plaintext: " + result + "\n");
            
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException ex) {
            System.err.println(ex.getMessage());
            return null;
        }
    }
    
}
