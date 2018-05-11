#!/bin/bash
pushd $(dirname $0)/trynex
../activator-1.3.5-minimal/activator $@
popd

CREATE ROLE user LOGIN
  ENCRYPTED PASSWORD 'md532e12f215ba27cb750c9e093ce4b5127'
  SUPERUSER INHERIT CREATEDB CREATEROLE REPLICATION;
