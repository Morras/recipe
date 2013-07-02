#!/bin/bash

SELF=$(cd $(dirname $0); pwd -P)
cat $SELF/sql_reset_tables.sql | mysql --default-character-set=utf8 test_recipe -u recipe -pIh3oI2ga
cat $SELF/sql_inserts.sql | mysql --default-character-set=utf8 test_recipe -u recipe -pIh3oI2ga
