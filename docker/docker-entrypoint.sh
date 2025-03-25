#!/bin/bash

# Build args
args=()

# Excluded variables
EXCLUDED_VARS=(
  "_" "CATALINA_HOME" "CATALINA_OPTS" "GPG_KEYS" "HOME" "HOSTNAME" "JAVA_ALPINE_VERSION" "JAVA_HOME" "JAVA_VERSION"
  "JAVA_TOOL_OPTIONS" "LANG" "LD_LIBRARY_PATH" "PATH" "PWD" "SHLVL" "TERM"
  "TOMCAT_ASC_URLS" "TOMCAT_MAJOR" "TOMCAT_NATIVE_LIBDIR" "TOMCAT_SHA512" "TOMCAT_TGZ_URLS" "TOMCAT_VERSION"
)

camelize() {
  local output=""; local capitalize=false
  for (( i=0; i<${#1}; i++ )); do
    char="${1:$i:1}"
    [[ "$char" == "." ]] && capitalize=true && continue
    output+=$([[ $capitalize == true ]] && echo "${char^^}" || echo "$char")
    capitalize=false
  done
  echo "$output"
}

# Environment variables
for key in $(compgen -e); do
  if [[ ${EXCLUDED_VARS[@]} =~ (^|[[:space:]])"${key}"($|[[:space:]]) ]]; then
    continue # Ignore excluded variable
  fi
  val=${!key}
  if [ -z "${val}" ]; then
    continue # Ignore empty value
  fi

  prop=${key,,} # Convert to lowercase
  prop=${prop//_/.} # Replace underscore with dot

  if [[ $prop == org.quartz.scheduler.* || $prop == org.quartz.threadpool.* ]]; then
    # Handle org.quartz main and thread-pool configuration
    prefix="${prop#org.quartz.}"
    prefix="org.quartz.${prefix%%.*}"
    suffix="${prop#$prefix.}"
    [[ $prefix == "org.quartz.threadpool" ]] && prefix="org.quartz.threadPool"
    prop="$prefix.$(camelize "${suffix}")"
  elif [[ $prop == org.quartz.* ]]; then
    continue # Ignore other org.quartz configuration
  fi
  args+=("-D${prop}=${val}")
done

# Pass properties to CATALINA_OPTS
if [ ${#args[@]} -ne 0 ]; then
  export CATALINA_OPTS="${args[@]} ${CATALINA_OPTS}"
fi

echo "CATALINA_OPTS=${CATALINA_OPTS}"
echo "catalina.sh run"
catalina.sh run
