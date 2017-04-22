FROM imixs/wildfly:latest

# Deploy artefact
COPY ./target/imixs-admin-*.war ${WILDFLY_DEPLOYMENT}/
