#!/bin/bash

# ensure sdk is installed
bin/install-elasticsearch-instance.sh

cp src/main/resources/elasticsearch.yml .es/config/

.es/bin/elasticsearch "$@"
