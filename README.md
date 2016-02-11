ElasticJasperServer
===================

This piece of the puzzle allows Jaspersoft Server to receive from Jaspersoft Studio and
execute reports that have ElasticSearch as datasource.

Compilation
-----------

Before compiling the server plugin you need to compile the [Elasticsearch results
parser](https://github.com/WedjaaOpen/ElasticParser) and the [Elasticsearch Jasper
Datasource Adapter](https://github.com/WedjaaOpen/ElasticJasper.git).

Clone those two projects and run in each of them:

```
mvn install
```

Once you are done you are ready to create the Elasticsearch Jasper Server adapter.

If it is the first time you compile this you need to launch firt the `validate`
step - this will download the Jasper Server distribution and extract the libraries
needed for compilation:

```
mvn validate
```

If you have already the proper version of the Jasper Server zip file you may 
drop it in the `downloads` directory of the project - you may need to create it. 
If the zip file is already there we won't download it from sourceforge.

You can now create the package you will need to configure the Jaspersoft Server:

```
mvn package
```

This will create a zip file - named `es-server-ds*.zip` - that you will need to
copy to your server. If you get an error that looks like:

```
Could not resolve dependencies for project net.wedjaa.elasticsearch:es-server-ds:jar:2.2.0
```

it means you haven'd run `mvn validate` at least once before trying to compile or
that the validate phase failed to download the Jasper Server package.

Installation
------------

The easy installation - that works if your Jasper server has no major customizations -
is to get the zip file created by the `mvn package` command - `es-server-ds-2.2.0.zip`
for example - and take it to the Jasper Server machine. Once on the machine unzip the
file:

```
unzip es-server-ds-2.2.0.zip
```

Enter the directory where the file was unzipped and run the installer:

```
cd es-server-ds-2.2.0
java -jar libs/es-server-ds-2.2.0.jar */path/to/jserver/installation*
```
Where */path/to/jserver/installation* is the path to the Jasper Server installation
in the application server; something like `/usr/local/tomcat/webapps/jasperserver`.

This will take care of configuring the server with the adapter, update the lucene
libraries and register the query language.

Once this is done you can restart the Jasper Server and test the new ES Adapter.

Manual Installation
-------------------

You can install the adapter by hand by following these steps

**Before proceeding with the installation, make sure that you have a working installation
of Jaspersoft Server: set it up, run it, try to run a couple of reports - check that it
behaves correctly.** If it stops working after installing this extension, at least you
know what broke it.

*JS_HOME* indicates the path where the actual Jaspersoft Server has been installed. For
example: if you deployed the server in a Tomcat application server that is installed in
**/opt/tomcat** your *JS_HOME* is most probably going to be
**/opt/tomcat/webapps/jasperserver**.

First of all stop the server. Then copy the adapter libraries you extracted from the
zip file - in the *libs/* path- under *JS_HOME/WEB-INF/lib*. Make sure you remove older
versions of existing libraries - like the lucene ones.

Copy the application configuration: *applicationContext-es-datasource.xml* - in
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
