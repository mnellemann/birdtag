## Bird Tagging

This application serves both as *ingest service* and *end-user service*.

Environment variables must be configured, see [application.properties](src/main/resources/application.properties) for a list.

## Upload image

```shell
curl -X POST -F "image=@scanner-packaging-01.jpg" -F "station=x" http://localhost:8080/ingest
```
