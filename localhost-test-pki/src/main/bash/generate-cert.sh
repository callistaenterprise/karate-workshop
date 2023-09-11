#!/bin/bash

CERTIFICATE_NAME=$1
CA_NAME=${2:-localCA}

# Create folder if necessary
mkdir -p target

# Generate private key
echo "Creating src/main/resources/certs/${CERTIFICATE_NAME}.key"
openssl genrsa -out src/main/resources/certs/${CERTIFICATE_NAME}.key 2048

# Create CSR
echo "Creating src/main/resources/certs/${CERTIFICATE_NAME}.csr"
openssl req -new -key src/main/resources/certs/${CERTIFICATE_NAME}.key \
  -config src/main/conf/${CERTIFICATE_NAME}.conf -out target/${CERTIFICATE_NAME}.csr

# Issue certificate from CSR
echo "Creating src/main/resources/certs/${CERTIFICATE_NAME}.crt"
openssl x509 -req -in target/${CERTIFICATE_NAME}.csr -CA src/main/resources/certs/${CA_NAME}.crt -CAkey \
  src/main/resources/certs/${CA_NAME}.key -out src/main/resources/certs/${CERTIFICATE_NAME}.crt -days 825 -sha256 \
  -passin pass: -extfile src/main/conf/${CERTIFICATE_NAME}.ext

# Export private and public keys into p12 format
echo "Packaging src/main/resources/certs/${CERTIFICATE_NAME}.{crt,key} into src/main/resources/certs/${CERTIFICATE_NAME}.p12"
openssl pkcs12 -export -out src/main/resources/certs/${CERTIFICATE_NAME}.p12 \
  -inkey src/main/resources/certs/${CERTIFICATE_NAME}.key -in src/main/resources/certs/${CERTIFICATE_NAME}.crt \
  -passout pass:secret -name ${CERTIFICATE_NAME}

# Convert into JKS format
echo "Packaging src/main/resources/certs/${CERTIFICATE_NAME}.{crt,key} into src/main/resources/certs/${CERTIFICATE_NAME}.jks"
keytool -v -importkeystore -srckeystore src/main/resources/certs/${CERTIFICATE_NAME}.p12 -srcstoretype PKCS12 \
  -destkeystore src/main/resources/certs/${CERTIFICATE_NAME}.jks \
  -deststoretype JKS -srcstorepass secret -deststorepass secret

# Delete CSR
rm -f target/${CERTIFICATE_NAME}.csr
