# Spring Boot View Injector
This library inject JavaScript CSS and Image(png and jpeg) to HTML and JavaScript.  
And it is NOT official parts of Awesome framework [Spring Boot](https://projects.spring.io/spring-boot/).
You write like that,

```html
<script src="js/app.js"></script>
```

ja/app.is
```js:app.js
alert("Spring Boot View Injector")
```

and it's converted at the time of execution.

```html
<script>alert("Spring Boot View Injector")</script>
```

Other case, Its inject HTML to JavaScript,

```js
new Vue({
	el: '#contents',
	/** @injectAsString("templates/other.html") */
	template: null,
	data: {
		message:"hello"
	}
});
```

templates/other.html
```html:templates/other.html
<div>{{message}}</div>
```

will convert to

```js
new Vue({
	el: '#contents',
	template: "<div>{{message}}</div>",
	data: {
		message:"hello"
	}
});
```

As you know, Spring Boot View Injector designed to Use with [Vue.js](https://jp.vuejs.org/index.html) for write template at the outside of JavaScript At first.

And next case, You can inject JavaScript to JavaScript.

```js
/** @inject("app/showAView.js") */
const showAView = null;
```

app/showAView.js
```js
new Vue({
	el: '#contents',
	/** @injectAsString("templates/other.html") */
	template: null,
	data: {
		message:"hello"
	}
});
```

will be

```js
const showAView = new Vue({
	el: '#contents',
	template: "<div>{{message}}</div>",
	data: {
		message:"hello"
	}
});;
```

It is for to create JavaScript as parts.

Spring Boot View Injector injects JavaScript, CSS with compress.
JavaScript compress with [closure-compiler-js](https://github.com/google/closure-compiler-js).
CSS compress with [YUI Compressor](http://yui.github.io/yuicompressor/). 

# Setup
Just load source as Gradle or Maven dependency.
This library puts on [Maven Central](https://mvnrepository.com/artifact/com.ukiuni/spring-boot-view-injector)

build.gradle
```
dependencies {
	runtime group: 'com.ukiuni', name:'spring-boot-view-injector', version:'0.0.10'
}
```

# Settings
write setting to application.yml (or application.config)

```
spring:
  injector:
    complessJS: true
    complessCss: true
    injectJSToHTML: true
    injectCssToHTML: true
    injectImageToHTML: true
    injectToJS: true
    useCache: true
```

| config | description | default | able | required | 
| --- | --- | --- | --- | --- |
| complessJS | Compless JavaScript or not | true | true or false | no |
| complessCss | Compless CSS or not | true | true or false | no |
| injectJSToHTML | Inject JavaScript to HTML or not | true | true or false | no |
| injectCssToHTML | Inject CSS to HTML or not | true | true or false | no |
| injectImageToHTML | Inject Image to HTML or not | true | true or false | no |
| injectToJS | Inject JavaScript to JavaScript or not | true | true or false | no |
| injectToJS | Cache injected resources or not | true | true or false | no |

Use this config for debug.
I believe these all are all able to true at production.

# License
Apache Software License 2.0
