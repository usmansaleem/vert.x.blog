# vert.x.blog (by Usman Saleem)
The blog software/server re-written using vert.x

## Build
`mvn clean package`
`docker build . -t <docker_tag>`

## Run 
### via Docker
`docker --itd --rm --name containername -p8080:8080 <docker_tag>`
### via commandline
`java -jar target/vertx-blog-full.jar`

## Relese process
Release process is managed by jgitflow plugin.

* `mvn jgitfow:release-start`
* `mvn jgitflow:release-finished -DnoDeploy=true`

## Change log
**Version 1.1**
 * Dockerize version.
 * Use bundled data.json
 
**Version 1.0**
 * Initial Version. Developed for Openshift Environment.
 
