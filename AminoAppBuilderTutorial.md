The AppBuilder is a subcomponent of Amino to build applications out of your source code. Currently it can build a JNLP app or native MacOSX app. We plan to support Windows MSI installers soon.

To use AppBundler you must first create an xml descriptor file to describe your application.  Here is an example used for the Leonardo app. You only need to specify the name of the app and at least one jar. Specify just the names of the jars, not the paths.

```
<?xml version="1.0" encoding="UTF-8"?>
<app name="Leonardo">
    <jar name="Sketch.jar" main-class="org.joshy.sketch.Main"/>
    <jar name="XMLLib.jar"/>
    <jar name="amino-core.jar"/>
    <jar name="commons-codec-1.4.jar"/>
    <jar name="apache-mime4j-0.6.jar"/>
    <jar name="commons-logging-1.1.1.jar"/>
    <jar name="httpclient-4.0.1.jar"/>
    <jar name="httpcore-4.0.1.jar"/>
    <jar name="httpcore-nio-4.0.1.jar"/>
    <jar name="httpmime-4.0.1.jar"/>
    <jar name="parboiled-0.9.7.3.jar"/>
    <jar name="twitter4j-core-2.1.4.jar"/>
    <jar name="JGoogleAnalytics_0.2.jar"/>
    <jar name="iText-2.1.7.jar"/>
</app>
```

AppBundler itself is a small Java application. You can invoke it from an ant file with the `java` task.

```
        <!-- build the mac bundle -->
        <java
            classpath="lib/AppBundler.jar;${amino.core.dir}/XMLLib.jar"
            classname="com.joshondesign.appbundler.Bundler" fork="true">

            <arg value="--file=bundler.xml"/>
            <arg value="--target=mac"/>
            <arg value="--outdir=dist/"/>
            <arg value="--jardir=${amino.core.dir}"/>
            <arg value="--jardir=build/jars/"/>
            <arg value="--jardir=lib/"/>
        </java>
```

The above code calls AppBundler to generate the app. The `--target=mac` argument tells it to build a MacOSX bundle. You can also specify `--target=jnlp` to build a JNLP.
