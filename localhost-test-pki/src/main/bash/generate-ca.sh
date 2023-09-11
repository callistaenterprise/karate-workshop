#!/bin/bash

CA_NAME=${1:-localCA}

# Create folder if necessary
mkdir -p src/main/resources/certs

# Generate CA private key
echo "Creating src/main/resources/certs/${CA_NAME}.key"
openssl genrsa -out src/main/resources/certs/${CA_NAME}.key 4096

# Create CA certificate
echo "Creating src/main/resources/certs/${CA_NAME}.crt"
openssl req -x509 -new -nodes -key src/main/resources/certs/${CA_NAME}.key \
  -config src/main/conf/${CA_NAME}.conf -sha256 -days 1024 -out src/main/resources/certs/${CA_NAME}.crt

# Package public key into truststore.jks
echo "Packaging src/main/resources/certs/${CA_NAME}.crt into src/main/resources/certs/truststore.jks"
keytool -import -v -trustcacerts -alias ${CA_NAME} -file src/main/resources/certs/${CA_NAME}.crt \
  -storepass secret -noprompt -keystore src/main/resources/certs/truststore.jks

# Package public key into truststore.p12
echo "Packaging src/main/resources/certs/${CA_NAME}.crt into src/main/resources/certs/truststore.p12"
openssl pkcs12 -export -nokeys -in src/main/resources/certs/${CA_NAME}.crt -out src/main/resources/certs/truststore.p12 \
  -passout pass:secret -name ${CA_NAME}