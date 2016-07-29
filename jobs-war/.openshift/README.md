The war for executing actual synchronization jobs

Need to scp cacerts file containing any CA to ${OPENSHIFT_DATA_DIR}cacerts

Add
```xml
<property name="javax.net.ssl.trustStore" value="${env.OPENSHIFT_DATA_DIR}cacerts" />
<property name="sync.config.war.url" value="${env.sync_config_war_url}" />
```
To openshift standalone.xml


