The war for executing actual synchronization jobs

Need to scp cacerts file containing any CA to ${OPENSHIFT_DATA_DIR}cacerts

Add
```xml
<property name="javax.net.ssl.trustStore" value="${env.OPENSHIFT_DATA_DIR}cacerts" />
<property name="sync.config.war.url" value="${env.sync_config_war_url}" />
```
To openshift standalone.xml

### enable custom key store

- copy your system default cacerts (/etc/pki/java/cacerts) file to a folder. e.g. ~/customKeyStore/cacerts
- import the root CA to this key store
  ```keytool -importcert -keystore cacerts -alias CA-RH-cert -storepass changeit -file RH-IT-Root-CA.crt```
- build the jobs war with ```-DcustomKeyStoreDir=~/customKeyStore```
  NOTE: it will copy everything under that folder to the war/WEB-INF/classes. Make sure you don't copy anything unnecessary.


