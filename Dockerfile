FROM imixs/wildfly:1.2.0

# Deploy artefact
COPY ./target/imixs-admin-*.war ${WILDFLY_DEPLOYMENT}/