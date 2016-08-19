package org.zanata.sync.util;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

public class SSHKeyGenTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private SSHKeyGen keyGen;

    @Before
    public void setUp() {
        keyGen = new SSHKeyGen();
    }

    @Test
    public void canGenerateKeyPair() throws Exception {
        File file = tempFolder.newFile();
        keyGen.generateKeyPair(file.getAbsolutePath(), "some comment");

        System.out.println("here");
    }

}
