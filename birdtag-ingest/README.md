## Bird Tagging - Ingest Service

Environment variables must be configured, see [application.properties](src/main/resources/application.properties) for a list.

## Upload image

curl -F image=@image.jpg http://localhost:8080/ingest
