### enable custom key store

- Need to scp cacerts file containing any CA to ${OPENSHIFT_DATA_DIR}cacerts
- Add
  ```xml
  <property name="javax.net.ssl.trustStore" value="${env.OPENSHIFT_DATA_DIR}cacerts" />
  ```
  To openshift standalone.xml
- copy your system default cacerts (/etc/pki/java/cacerts) file to a folder. e.g. ~/customKeyStore/cacerts
- import the root CA, Eng-CA to this key store
  ```keytool -importcert -keystore cacerts -alias CA-RH-cert -storepass changeit -file RH-IT-Root-CA.crt```
- build the jobs war with ```-DcustomKeyStoreDir=~/customKeyStore```
  NOTE: it will copy everything under that folder to the war/WEB-INF/classes. Make sure you don't copy anything unnecessary.



### add red hat CA into the openshift cartridge

curl https://password.corp.redhat.com/RH-IT-Root-CA.crt \
    -o /etc/pki/ca-trust/source/anchors/RH-IT-Root-CA-2015.crt && \
    curl https://password.corp.redhat.com/cacert.crt \
    -o /etc/pki/ca-trust/source/anchors/RH-IT-Root-CA-2009.crt && \
    update-ca-trust