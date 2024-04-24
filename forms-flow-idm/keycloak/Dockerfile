FROM quay.io/keycloak/keycloak:23.0.3

ADD ./bg-theme-1.0.0.jar /opt/keycloak/providers/bg-theme-1.0.0.jar
ADD ./eauth2-provider-1.0.0.jar /opt/keycloak/providers/eauth2-provider-1.0.0.jar
ADD ./eforms-theme-1.0.0.jar /opt/keycloak/providers/eforms-theme-1.0.0.jar
ADD ./sm-pin-provider-1.0.0.jar /opt/keycloak/providers/sm-pin-provider-1.0.0.jar
ADD ./sm-theme-1.0.0.jar /opt/keycloak/providers/sm-theme-1.0.0.jar
ADD ./eServices-theme-1.0.0.jar /opt/keycloak/providers/eServices-theme-1.0.0.jar
ADD ./digitalSofia-theme-1.0.0.jar /opt/keycloak/providers/digitalSofia-theme-1.0.0.jar
ADD ./sm-firebase-provider-1.0.0.jar /opt/keycloak/providers/sm-firebase-provider-1.0.0.jar
ADD ./config/quarkus.properties /opt/keycloak/conf/quarkus.properties
ADD ./config/cache-ispn.xml /opt/keycloak/conf/cache-ispn.xml

RUN /opt/keycloak/bin/kc.sh build
ENV KC_PROXY=edge

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"  , "start-dev" , "-Dkeycloak.saml.deprecated.encryption=true" ]
