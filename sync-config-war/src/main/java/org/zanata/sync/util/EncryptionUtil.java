/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.sync.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * TODO pahuang check this http://www.macs.hw.ac.uk/~ml355/lore/pkencryption.htm
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EncryptionUtil {
    private static Cipher cipher = makeCipher();

    private static Cipher makeCipher() {
        try {
            return Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("can not create cipher", e);
        }
    }

    private final SecretKeySpec key;

    public EncryptionUtil(byte[] keyBytes) {
        // java only allow 128bit (16 chars) in key by default (seems to vary between openJDK and sun JDK)
        byte[] validKeyBytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00 };
        // make sure we have an array of 16 length
        System.arraycopy(keyBytes, 0, validKeyBytes, 0, Math.min(keyBytes.length, 16));
        // it seems to do some padding internally and will make the keyBytes
        // become 192 bits. Here we set the length to 16 to force it to 128 bit
        key = new SecretKeySpec(validKeyBytes, 0, 16, "AES");
    }


    public String encrypt(String input) {
        // encryption pass
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(input.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString((encrypted));
        } catch (InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | UnsupportedEncodingException e) {
            throw new IllegalStateException("failed to encrypt input", e);
        }
    }



    public String decrypt(String cipherText) {
        // decryption pass
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(
                    cipher.doFinal(Base64.getDecoder().decode(cipherText)));
        } catch (InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException e) {
            throw new IllegalStateException("failed to decrypt value", e);
        }

    }
}
