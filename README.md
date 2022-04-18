Weimarnetz Registrator
====

[![Build Status](https://travis-ci.com/weimarnetz/registrator.svg?branch=master)](https://travis-ci.com/weimarnetz/registrator) [![codecov](https://codecov.io/gh/weimarnetz/registrator/branch/master/graph/badge.svg)](https://codecov.io/gh/weimarnetz/registrator)

Prerequisites
----
* jdk installed
* docker installed

About
----
This is an Spring-Boot application that is already configured to support a lot of tools and plugins i like to use in my daily business.

Things that are configured already:
* [Actuator](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready)
	* adds health check, jms, metrics, loggers, dependencies, ... endpoints
* deployment via [Ansible](https://www.ansible.com/)
	* deploys the application into a vagrant box for testing behaviour - ready to only adjust hosts and ssh config to
	  have a server ready deployment. including serverside installation of:
		* jdk

Run locally
-----

* Start a local postgresql instance with `mvn docker:start`
* Start the service with ```mvn spring-boot:run```

Find generated docs at https://weimarnetz.github.io/registrator/
