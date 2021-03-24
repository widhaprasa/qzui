Qzui
====

Qzui, a basic REST and Web front end over [Quartz Scheduler](http://quartz-scheduler.org/).

## Features

### Simple UI

* Simple UI to get the list of jobs and their triggers
* Filter by group
* Delete (cancel) a job

### Jobs type

* Log job: Log something, mainly used for testing
* Shell job: Execute shell script
* HTTP job: Make HTTP request

### Timers

* Right 'now'
* At specified time
* Using cron syntax
* Simple schedule (every)

### REST API

* Create a new job
* Get job detail
* Get list of jobs by group
* Get list of all jobs
* Delete a job
* Delete jobs by group
* Delete all jobs

### Easy hacking

As of Feb 2014, Qzui has been developed in a couple of hours, the server part consists of less than 500 LOC:

```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            10            110             63            480
```


## Hacking


### Get, Build and Run

```
git clone https://github.com/xhanin/qzui.git
cd qzui
mvn package
```

This will produce a war in the `srv/target/` directory that you can deploy in any servlet 2.5+ container.

You can also run the server using [RESTX](http://restx.io/) with `restx app run` for dev mode (with auto compile for easy hacking), and `restx app run --prod` for production mode (launch it in `srv` directory).

To launch it from your IDE use the `AppServer` class.

### Add new job types

Adding a new job type is simple, take example from existing ones:

[HttpJobDefinition](https://github.com/xhanin/qzui/blob/master/srv/src/main/java/qzui/HttpJobDefinition.java)

And do not forget to add the type json value in [JobDescriptor](https://github.com/xhanin/qzui/blob/master/srv/src/main/java/qzui/JobDescriptor.java)

### Add new triggers

Hack the [TriggerDescriptor](https://github.com/xhanin/qzui/blob/master/srv/src/main/java/qzui/TriggerDescriptor.java) class.

### Hack UI

The basic UI is developed using AngularJS + TW Boostrap, check [ui/app](https://github.com/xhanin/qzui/tree/master/ui/app).

### Configure

You can setup the quartz configuration as you like, following [Quartz documentation](http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/)
We strongly recommend setting up a jobstore if you don't want to lose your jobs at each server restart.

## Usage

When the server is launched, open your browser at http://localhost:8080/ and you will get the list of jobs.

To create a job use the REST API: do a POST on http://localhost:8080/api/groups/:group/jobs

To do so you can use the RESTX API console (login admin/juma) at http://127.0.0.1:8080/api/@/ui/api-docs/#/

Note that jobs MUST have unique names.

### Examples:

#### HTTP Job, scheduled at fixed date/time:

```
{
  "type":"http",
  "name":"google-humans",
  "method":"GET",
  "url":"http://www.google.com/humans.txt",
  "triggers": [
        {"when":"2014-11-05T13:15:30Z"}
  ]
}
```

#### HTTP Job, scheduled now.

```
{
  "type":"http",
  "name":"google-humans",
  "method":"GET",
  "url":"http://www.google.com/humans.txt",
  "triggers": [
        {"when":"now"}
  ]
}
```

#### HTTP Job, scheduled using cron syntax.

```
{
  "type":"http",
  "name":"google-humans",
  "method":"GET",
  "url":"http://www.google.com/humans.txt",
  "triggers": [
        {"cron":"0/2 * * * * ?"}
  ]
}
```

#### HTTP Job, scheduled using every (in secs).

```
{
  "type":"http",
  "name":"google-humans",
  "method":"GET",
  "url":"http://www.google.com/humans.txt",
  "triggers": [
        {"every":60}
  ]
}
```

## Why?

Because I don't like embedding a job scheduler inside my web application server, mainly because scaling a web application server should be done easily by adding new nodes, while a scheduler cluster is much harder to setup and most of the time a single scheduler server can handle tremendous number of jobs, especially if jobs are performed asynchronously.

Therefore I tend to make the web application schedule a job with a REST API call on Qzui, then Qzui call it back when scheduled. This is similar to how [Google App Engine Scheduled Tasks](https://developers.google.com/appengine/docs/java/config/cron) are designed.

And also because it's fun to develop with [RESTX](http://restx/io) + [AngularJS](http://angularjs.org/), and can also be used as an example of how to embed Quartz in a RESTX app (look at [QuartzModule](https://github.com/xhanin/qzui/blob/master/srv/src/main/java/qzui/QuartzModule.java)).

## Production ready?

Quartz is production ready, and it's the component doing the heavy lifting.

## Screenshots

![Qzui UI](https://i.cloudup.com/rA5oWU9hqd-2000x2000.png)

![Qzui post jobs](https://i.cloudup.com/ZCkwMOtVpr-3000x3000.png)
