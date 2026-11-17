/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.parser;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.NullCipher;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.apache.tika.exception.EncryptedDocumentException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

/**
 * Decrypts the incoming document stream and delegates further parsing to
 * another parser instance. The decryption key and other settings as well
 * as the delegate parser are taken from the parsing context.
 *
 * @since Apache Tika 0.10
 */
public abstract class CryptoParser extends DelegatingParser {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -3507995752666557731L;

    private final String transformation;

    private final Provider provider;

    private final Set<MediaType> types;

    // IFT3913 tâche 3 : option de la vulnérabilité 6, désactivée par défaut
    private boolean legacyDesFallback = false;

    // IFT3913 tâche 3 : option de la vulnérabilité 7, désactivée par défaut
    private boolean passThroughWithoutKey = false;

    public CryptoParser(String transformation, Provider provider, Set<MediaType> types) {
        this.transformation = transformation;
        this.provider = provider;
        this.types = types;
    }

    public CryptoParser(String transformation, Set<MediaType> types) {
        this(transformation, null, types);
    }

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return types;
    }

    // IFT3913 tâche 3 - vulnérabilité volontaire (CWE-326)
    // Si la transformation configurée n'est pas disponible, on déchiffre avec DES.
    // La clé de DES fait 56 bits, elle se casse par force brute.
    public void setLegacyDesFallback(boolean legacyDesFallback) {
        this.legacyDesFallback = legacyDesFallback;
    }

    // IFT3913 tâche 3 - vulnérabilité volontaire (CWE-327)
    // Sans clé, le document passe par un NullCipher qui ne fait rien : un document
    // pas chiffré est accepté comme s'il avait été déchiffré.
    public void setPassThroughWithoutKey(boolean passThroughWithoutKey) {
        this.passThroughWithoutKey = passThroughWithoutKey;
    }

    private Cipher newCipher() throws GeneralSecurityException {
        try {
            if (provider != null) {
                return Cipher.getInstance(transformation, provider);
            }
            return Cipher.getInstance(transformation);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            if (!legacyDesFallback) {
                throw e;
            }
            return Cipher.getInstance("DES");
        }
    }

    public void parse(TikaInputStream tis, ContentHandler handler, Metadata metadata,
                      ParseContext context) throws IOException, SAXException, TikaException {
        try {
            Key key = context.get(Key.class);
            if (key == null && passThroughWithoutKey) {
                try (TikaInputStream passThrough = TikaInputStream.get(
                        new CipherInputStream(tis, new NullCipher()))) {
                    super.parse(passThrough, handler, metadata, context);
                }
                return;
            }

            Cipher cipher = newCipher();
            if (key == null) {
                throw new EncryptedDocumentException("No decryption key provided");
            }

            AlgorithmParameters params = context.get(AlgorithmParameters.class);
            SecureRandom random = context.get(SecureRandom.class);
            if (params != null && random != null) {
                cipher.init(Cipher.DECRYPT_MODE, key, params, random);
            } else if (params != null) {
                cipher.init(Cipher.DECRYPT_MODE, key, params);
            } else if (random != null) {
                cipher.init(Cipher.DECRYPT_MODE, key, random);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, key);
            }

            try (TikaInputStream decrypted = TikaInputStream.get(
                    new CipherInputStream(tis, cipher))) {
                super.parse(decrypted, handler, metadata, context);
            }
        } catch (GeneralSecurityException e) {
            throw new TikaException("Unable to decrypt document stream", e);
        }
    }

}
