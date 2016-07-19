package org.zanata.sync.dao;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.zanata.sync.component.AppConfiguration;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.util.EncryptionUtil;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Decorator
public class SyncWorkConfigSerializerEncryptionDecorator
        implements SyncWorkConfigSerializer {
    @Inject
    @Delegate
    private SyncWorkConfigSerializerImpl delegate;

    @Inject
    private AppConfiguration appConfiguration;

    @Override
    public SyncWorkConfig fromYaml(File file) {
        return readYamlAndDecryptIfNeeded(delegate.fromYaml(file));
    }

    private SyncWorkConfig readYamlAndDecryptIfNeeded(
            SyncWorkConfig directConvert) {
        List<String> fieldsNeedEncryption =
                appConfiguration.getFieldsNeedEncryption();
        String encryptionKey = directConvert.getEncryptionKey();

        if (fieldsNeedEncryption.isEmpty() ||
                Strings.isNullOrEmpty(encryptionKey)) {
            return directConvert;
        }

        // start the decryption
        EncryptionUtil encryptionUtil =
                new EncryptionUtil(encryptionKey.getBytes(Charsets.UTF_8));

        BiFunction<String, String, String> decryptFunc =
                (key, value) -> encryptionUtil.decrypt(value);
        for (String field : fieldsNeedEncryption) {
//            directConvert.getSrcRepoPluginConfig().computeIfPresent(field,
//                    decryptFunc);
        }

        return directConvert;
    }

    @Override
    public String toYaml(SyncWorkConfig syncWorkConfig) {
        List<String> fieldsNeedEncryption =
                appConfiguration.getFieldsNeedEncryption();
        String encryptionKey = syncWorkConfig.getEncryptionKey();

        if (fieldsNeedEncryption.isEmpty() ||
                Strings.isNullOrEmpty(encryptionKey)) {
            return delegate.toYaml(syncWorkConfig);
        }

        // start the encryption
        EncryptionUtil encryptionUtil =
                new EncryptionUtil(encryptionKey.getBytes(Charsets.UTF_8));

        BiFunction<String, String, String> encryptFunc =
                (key, value) -> encryptionUtil.encrypt(value);
        for (String field : fieldsNeedEncryption) {
//            syncWorkConfig.getSrcRepoPluginConfig().computeIfPresent(field,
//                    encryptFunc);
        }

        return delegate.toYaml(syncWorkConfig);
    }

    @Override
    public SyncWorkConfig fromYaml(String yaml) {
        return readYamlAndDecryptIfNeeded(delegate.fromYaml(yaml));
    }

}
