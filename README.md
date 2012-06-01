# Vraptor Hypermedia relations enhanced!

## How to install?
Just add this entry to your pom.xml:
```xml
  <dependency>
      <groupId>com.github.filipesperandio.vraptor</groupId>
      <artifactId>vraptor-hypermedia</artifactId>
      <version>3.4.0</version>
  </dependency>
```

... or download the jar and add it manually to your project:
http://search.maven.org/remotecontent?filepath=com/github/filipesperandio/vraptor/vraptor-hypermedia/3.4.0/vraptor-hypermedia-3.4.0.jar

Update web.xml accordingly:
```xml
  <context-param>
    <param-name>br.com.caelum.vraptor.provider</param-name>
    <param-value>com.github.filipesperandio.vraptor.hypermedia.HypermediaProvider</param-value>
  </context-param>
```

## What is does?

It changes the relations model in order to help your javascript code to be more readable an easy to do.

Instead of a json like the following one using RestfulSerializationJSON...

```javascript
  link : {
    rel : "delete",
    href : "http://host/resource/id"
  }
```

... you get a JSON like this:

```javascript
  link : {
    "delete" : {
      method : "DELETE",
      url : "/resource/id"
    }
  }
```

This model is pretty much complient to all common ajax javascript APIs available. Good examples are Angular and jQuery.
Ex.:
```javascript
  $.ajax(jsmodel.link.delete);
```



