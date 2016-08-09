The OpenShift `jbosseap` cartridge documentation can be found at:

http://openshift.github.io/documentation/oo_cartridge_guide.html#jbosseap


TODO:
- techdebt: npm WARN deprecated minimatch@2.0.10: Please update to minimatch
  3.0.2 or higher to avoid a RegExp DoS issue (used by babel-core)
- server startup log:
  05:01:32,167 WARN  [org.jboss.weld.Validator] (MSC service thread 1-3) WELD-001478: Interceptor class org.apache.deltaspike.security.impl.extension.SecurityInterceptor is enabled for the application and for the bean archive sync.war/WEB-INF/classes. It will only be invoked in the @Priority part of the chain.
  05:01:32,167 WARN  [org.jboss.weld.Validator] (MSC service thread 1-5) WELD-001478: Interceptor class org.apache.deltaspike.security.impl.extension.SecurityInterceptor is enabled for the application and for the bean archive jobs.war/WEB-INF/classes. It will only be invoked in the @Priority part of the chain.
  05:01:32,170 WARN  [org.jboss.weld.Validator] (MSC service thread 1-5) WELD-001478: Interceptor class org.apache.deltaspike.security.impl.extension.SecurityInterceptor is enabled for the application and for the bean archive jobs.war/WEB-INF/lib/deltaspike-security-module-impl-1.5.1.jar. It will only be invoked in the @Priority part of the chain.
  05:01:32,172 WARN  [org.jboss.weld.Validator] (MSC service thread 1-3) WELD-001478: Interceptor class org.apache.deltaspike.security.impl.extension.SecurityInterceptor is enabled for the application and for the bean archive sync.war/WEB-INF/lib/deltaspike-security-module-impl-1.5.1.jar. It will only be invoked in the @Priority part of the chain.
  05:01:33,727 WARN  [org.jboss.weld.Validator] (MSC service thread 1-5) WELD-001471: Interceptor method start defined on class org.zanata.sync.quartz.CronTrigger is not defined according to the specification. It should not throw org.quartz.SchedulerException, which is a checked exception.
  	at org.zanata.sync.quartz.CronTrigger.start(CronTrigger.java:57)
    StackTrace
  05:01:33,732 WARN  [org.jboss.weld.Validator] (MSC service thread 1-3) WELD-001471: Interceptor method start defined on class org.zanata.sync.quartz.CronTrigger is not defined according to the specification. It should not throw org.quartz.SchedulerException, which is a checked exception.
  	at org.zanata.sync.quartz.CronTrigger.start(CronTrigger.java:57)


## Architectural diagram
```

             +-----------------+                        +------------------+
+----------+ | Sync UI App     |                        | Job execution App|
|Single    | | (artifact from  |                        | (artifact from   |
|Page      | | sync-config-war |                        |  jobs-war module)+-+
|App       | | module)         |                        |                  | +-+
|(React.js)| |                 |                        | -actual work done| | |
|          +->                 |                        |  here(git,zanata)| | |
|          | |                 |                        | -faceless REST   | | |
|          <-+   +----------+  |     quartz job run     |  only            | | |
|          | |   | quartz   +-------------------------> | -scalable        | | |
|          | |   | scheduler|  |  triggers a REST call  |                  | | |
+----------+ |   +----------+  |                        |                  | | |
             |                 |      REST post back    |                  | | |
             |                 | <--------------------+ |                  | | |
             +--+----------+---+     job run status     +-+----------------+ | |
                |          ^                              |                  | |
                |          |                              ++-----------------+ |
                v          |                               |                   |
               +----------+-+                              +-------------------+
               |Database    |
               |(Job config)|
               |(Job run    |
               | history)   |
               |...         |
               +------------+
```

### Local Installation

#### install to your local jboss

Update your standalone.xml:

- add system properties:
  ```<property name="zanata.support.oauth" value="true"/>``` and make sure you
  are running a Zanata version that supports OAuth (current master is)
- add a new datasource and bind it to JNDI ```java:jboss/datasources/syncDS```
  (don't forget to create a new database too)
- under ```<subsystem xmlns="urn:jboss:domain:web">```
```diff
- <connector name="http" protocol="HTTP/1.1" scheme="http" socket-binding="http"/>
+ <connector name="http" protocol="org.apache.coyote.http11.Http11NioProtocol"
+   scheme="http" socket-binding="http"/>
```

#### using docker

- run ```docker-img-build.sh``` to build the docker image
- run ```rundb.sh -c``` to run and create the database container.
  (-c will create the database so you only need to run it for the first time)
- run ```rundev.sh``` to run the actual wildfly server and it will create a
  folder at *$HOME/docker-volumes/sync-deployments* as the wildfly deployments
  volume. The war files will also be copied over to there.
  NOTE: the first deployment will fail because of an issue with liquibase and
  postgres [1]. Just change the sync.war.faildeploy file to sync.war.dodeploy to
  let it redeploy again. It will be fine afterwards.

[1] https://liquibase.jira.com/browse/CORE-2846

### Tips

- use ```./build.sh``` to build and deploy to your local server (-H to see help)
- use ```./openshift_deploy_config_war.sh``` to deploy sync-config-war artifact
  to openshift.
- use ```./openshift_deploy_jobs_war.sh``` to deploy jobs-war artifact to
  openshift.
