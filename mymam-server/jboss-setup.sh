#!/bin/bash

# MyMAM - Open Source Digital Media Asset Management.
# http://www.mymam.net
#
# Copyright 2013, MyMAM contributors as indicated by the @author tag.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

###########################################################################
# This script configures JBoss for MyMAM
# @author fstab
###########################################################################

# Before running this script, you need to:

# unzip jboss-as-7.1.1.Final.zip
# cp postgresql-9.2-1002.jdbc4.jar ./jboss-as-7.1.1.Final/standalone/deployments
# ./jboss-as-7.1.1.Final/bin/standalone.sh

cat <<EOF | ~/jboss-as-7.1.1.Final/bin/jboss-cli.sh

    connect
    batch

    # create the mymamDS data source for postgres
 
    data-source add \
        --name=mymamDS \
        --driver-name=postgresql-9.2-1002.jdbc4.jar \
        --connection-url=jdbc:postgresql://localhost:5432/MYMAM \
        --jndi-name=java:jboss/jdbc/mymamDS \
        --user-name=postgres \
        --password=postgres \
    )

    data-source enable --name=mymamDS

    # Create security domain for form based authentication.
    # The security domain must be referenced from jboss-web.xml

    /subsystem=security/security-domain=mymam-security-domain:add

    /subsystem=security/security-domain=mymam-security-domain/authentication=classic:add( \
        login-modules=[ \
            { \
                "code" => "Database", \
                "flag" => "required", \
                "module-options" => [ \
                    ("dsJndiName"=>"java:jboss/jdbc/mymamDS"), \
                    ("principalsQuery"=>"select hashedpassword from user_table where username=?"), \
                    ("rolesQuery" => "select name, 'Roles' from role join role_user_table on role.id = role_user_table.roles_id join user_table on user_table.id = role_user_table.users_id where user_table.username=?"), \
                    ("hashAlgorithm" => "MD5"), \
                    ("hashEncoding" => "base64") \
                ] \
            } \
        ] \
    ) {allow-resource-service-restart=true}


    # enable logging for form based authentication

#    /subsystem=logging/logger=org.jboss.security:add(level=TRACE)
#    /subsystem=logging/logger=org.jboss.as.web.security:add(level=TRACE)
#    /subsystem=logging/logger=org.apache.catalina:add(level=TRACE)
#    /subsystem=logging/console-handler=CONSOLE:write-attribute(name=level, value=TRACE)

    run-batch
    :reload
EOF


    #/subsystem=datasources/jdbc-driver=postgresql:add( \
    #    driver-name=postgresql-9.2-1002.jdbc4.jar, \
    #    driver-module-name=org.postgresql.jdbc, \
    #    driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource \
 
# Add a non-XA datasource
# 
# data-source enable --name=mymamDS
# 
# # create security domain for form based authentication
# 
# /subsystem=security/security-domain=mymam-security-domain:add
# /subsystem=security/security-domain=mymam-security-domain/authentication=classic:add( \
#     login-modules=[ \
#         { \
#             "code" => "Database", \
#             "flag" => "required", \
#             "module-options" => [ \
#                 ("unauthenticatedIdentity"=>"guest"), \
#                 ("dsJndiName"=>"java:jboss/jdbc/mymamDS"), \
#                 ("principalsQuery"=>"select hashedpassword from user_table where username=?"), \
#                 ("rolesQuery" => "select name, 'Roles' from role join role_user_table on role.id = role_user_table.roles_id join user_table on user_table.id = role_user_table.users_id where user_table.username=?"), \
#                 ("hashAlgorithm" => "MD5"), \
#                 ("hashEncoding" => "base64") \
#             ] \
#         } \
#     ] \
# ) {allow-resource-service-restart=true}
# 
