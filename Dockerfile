FROM imixs/wildfly:1.2.3

# Deploy artefact
COPY ./target/imixs-admin-*.war ${WILDFLY_DEPLOYMENT}/