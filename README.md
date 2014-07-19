
solrpanel
---------

A maven solr template for local development.


- MAVEN_OPTS="-Xms512m -Xmx1024m -Dsolr.solr.home=/Users/dapurv5/Dropbox/ProgramFiles/solr-ca-4.3.0/example/solr -Dsolr.top.dir=/Users/dapurv5/Dropbox/ProgramFiles/solr-ca-4.3.0/example/solr" mvn -Dmaven.test.skip=true clean package tomcat7:run-war-only

- http://localhost:8080/solr/bloomanalytics-core/browse
