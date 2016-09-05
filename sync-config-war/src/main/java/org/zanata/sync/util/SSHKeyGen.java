/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

/**
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SSHKeyGen {
    private static final Logger log = LoggerFactory.getLogger(SSHKeyGen.class);
    private static final int KEY_TYPE = KeyPair.RSA;




    protected void generateKeyPair(String fileName, String comment) {
        JSch jsch = new JSch();
        try (ByteArrayOutputStream priOut = new ByteArrayOutputStream();
                ByteArrayOutputStream pubOut = new ByteArrayOutputStream()) {
            KeyPair keyPair = KeyPair.genKeyPair(jsch, KEY_TYPE);

            keyPair.writePrivateKey(fileName);

            keyPair.writePrivateKey(priOut);
            log.debug("private key: {}", priOut.toString("UTF-8"));

            keyPair.writePublicKey(fileName + ".pub", comment);
            keyPair.writePublicKey(pubOut, comment);
            log.debug("public key: {}", pubOut.toString("UTF-8"));

            log.info("Finger print: {}", keyPair.getFingerPrint());

            keyPair.dispose();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
