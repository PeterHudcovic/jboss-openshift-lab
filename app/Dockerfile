FROM quay.io/wildfly/wildfly:41.0.1.Final-jdk21

ADD --chown=jboss:root https://jdbc.postgresql.org/download/postgresql-42.7.13.jar /opt/jboss/wildfly/standalone/deployments/postgresql-42.7.13.jar

COPY --chown=jboss:root target/donation-app.war /opt/jboss/wildfly/standalone/deployments/donation-app.war

COPY --chown=jboss:root configure-datasource.cli /opt/jboss/wildfly/configure-datasource.cli

USER root

RUN chmod 0640 /opt/jboss/wildfly/standalone/deployments/postgresql-42.7.13.jar /opt/jboss/wildfly/standalone/deployments/donation-app.war /opt/jboss/wildfly/configure-datasource.cli

USER jboss

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/configure-datasource.cli

USER root

RUN rm -f /opt/jboss/wildfly/configure-datasource.cli && rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history /opt/jboss/wildfly/standalone/log/* /opt/jboss/wildfly/standalone/tmp/* /opt/jboss/wildfly/standalone/data/* && chgrp -R 0 /opt/jboss/wildfly/standalone && chmod -R g=u /opt/jboss/wildfly/standalone

USER jboss