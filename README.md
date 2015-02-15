ElasticJasperServer
===================

This piece of the puzzle allows Jaspersoft Server to receive from Jaspersoft Studio and
execute reports that have ElasticSearch as datasource. 

Compilation
-----------

To compile this software you will need:

  - Compile time JARs
    * jasperserver-api-metadata-5.6.*x*.jar
    * jasperreports-5.6.*x*.jar
  - ElasticJasper project open in your Eclipse workspace
  
You can get the first two JARs from the Jaspersoft Server deployment and clone and compile
[ElasticJasper](https://github.com/WedjaaOpen/ElasticJasper "ElasticJasper Repository")
by following the instructions there.

If you are lazy or Eclipse impaired you can use the pre-compiled libraries - 
*es-adapter.jar*, *elasticparser-0.5.1-SNAPSHOT.jar* and *es-server-ds.jar* - that you will find in this repository. 
Be aware that these binaries may not always be up to date.

Installation
------------

The installation of the server component is not as straight forward as installing the
plugin in the Jaspersoft Studio and requires a bit of care since you may break your
working installation of Jaspersoft Server. This may not be a problem if you're setting
one up just for this.

**Before proceeding with the installation, make sure that you have a working installation
of Jaspersoft Server: set it up, run it, try to run a couple of reports - check that it
behaves correctly.** If it stops working after installing this extension, at least you
know what broke it.

*JS_HOME* indicates the path where the actual Jaspersoft Server has been installed. For 
example: if you deployed the server in a Tomcat application server that is installed in
**/opt/tomcat** your *JS_HOME* is most probably going to be
**/opt/tomcat/webapps/jasperserver**.

First of all stop the server. Then copy the adapter libraries - *es-adapter.jar*, *elasticparser-0.5.1-SNAPSHOT.jar*,
 and *es-server-ds.jar* - under *JS_HOME/WEB-INF/lib*

Then you need to remove the *lucene* libraries installed with Jaspersoft Server. This is
because they are an old version and we need a newer one.

Copy the libraries needed by *elasticsearch* in *JS_HOME/WEB-INF/lib*. These libraries,
it's not a coincidence, are the same ones you have bundled in the 
[ElasticSearchOSGI](https://github.com/WedjaaOpen/ElasticSearchOSGI "ES OSGI Bundle"):

 - elasticsearch-1.1.2.jar
 - lucene-analyzers-common-4.7.2.jar
 - lucene-codecs-4.7.2.jar
 - lucene-core-4.7.2.jar
 - lucene-grouping-4.7.2.jar
 - lucene-highlighter-4.7.2.jar
 - lucene-join-4.7.2.jar
 - lucene-memory-4.7.2.jar
 - lucene-misc-4.7.2.jar
 - lucene-queries-4.7.2.jar
 - lucene-queryparser-4.7.2.jar
 - lucene-sandbox-4.7.2.jar
 - lucene-spatial-4.7.2.jar
 - lucene-suggest-4.7.2.jar
 - spatial4j-0.4.1.jar
 
Copy the application configuration - *applicationContext-es-datasource.xml* - in
*JS_HOME/WEB-INF* and *es_datasource.properties* in  *JS_HOME/WEB-INF/bundles*
 
It's not time to do some editing.
 
Register the query language in the REST points configuration. 
Open *JS_HOME/WEB-INF/applicationContext-rest-services.xml* and find this bit:
 
    <util:list id="queryLanguagesCe">
        <value>sql</value>
        <value>hql</value>
        <value>domain</value>
        <value>HiveQL</value>
        <value>MongoDbQuery</value>
        <value>cql</value>
    </util:list>

and change it to:

    <util:list id="queryLanguagesCe">
        <value>sql</value>
        <value>hql</value>
        <value>domain</value>
        <value>HiveQL</value>
        <value>MongoDbQuery</value>
        <value>elasticsearch</value>
        <value>cql</value>
    </util:list>

Then it's time to edit *JS_HOME/WEB-INF/classes/jasperreports.properties* - add, at the
very top of the file:
  
    net.sf.jasperreports.query.executer.factory.elasticsearch=net.wedjaa.jasper.elasticsearch.query.ESQueryExecuterFactory
    
Now you can restart the server and test some ElasticSearch based reports.
 
Support
-------
 
This software is released as is. We make no claim that it will do anything useful and 
it may potentially do harm to your self confidence. We will however keep an eye on the 
issues you open on GitHub and try an fix whatever it's broken.

We do offer professional services and support in case you need.

  
  
  
