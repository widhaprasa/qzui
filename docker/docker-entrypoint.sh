#!/bin/bash

# Build args
args=()

# Excluded variables
EXCLUDED_VARS=(
  "_" "CATALINA_HOME" "CATALINA_OPTS" "GPG_KEYS" "HOME" "HOSTNAME" "JAVA_ALPINE_VERSION" "JAVA_HOME" "JAVA_VERSION"
  "LANG" "LD_LIBRARY_PATH" "PATH" "PWD" "SHLVL" "TERM"
  "TOMCAT_ASC_URLS" "TOMCAT_MAJOR" "TOMCAT_NATIVE_LIBDIR" "TOMCAT_SHA512" "TOMCAT_TGZ_URLS" "TOMCAT_VERSION"
)

# Environment variables
for key in $(compgen -e); do
  if [[ ${EXCLUDED_VARS[@]} =~ (^|[[:space:]])"${key}"($|[[:space:]]) ]]; then
    continue # Exclude variable
  fi
  val=${!key}
  if [ -z "${val}" ]; then
    continue # Exclude empty value
  fi

  prop=${key,,} # Convert to lowercase
  prop=${prop//_/.} # Replace underscore with dot

  if [[ $prop == org.quartz.* ]]; then
    continue # Ignore org.quartz.* properties
  elif [[ $prop == qzui.jobstore.* ]]; then
    prop=${prop/qzui.jobstore/qzui.jobStore} # Replace qzui.jobstore
    args+=("-D${prop}=${val}")
  else
    args+=("-D${prop}=${val}")
  fi
done

# Pass properties to CATALINA_OPTS
if [ ${#args[@]} -ne 0 ]; then
  export CATALINA_OPTS="${args[@]} ${CATALINA_OPTS}"
fi

echo "CATALINA_OPTS=${CATALINA_OPTS}"
echo "catalina.sh run"
catalina.sh run
