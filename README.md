## Required
1. MongoDB
    - settings: `application.properties` spring.data.mongodb.uri

## RUN
- start directly
    ```
    # MongoDB required
    # settings: `application.properties` spring.data.mongodb.uri

    $ mvn spring-boot:run
    ```

- start with docker
    ```
    # Docker required

    $ docker stack up -c ./docker-compose.yml demo
    ```

### index.html
`http://localhost:8080/static/index.html`

### CRUD Demo
`http://localhost:8080/static/member-demo.html`
1. Router Style
    - WebFlux Demo (router style)
    - default enable
2. Controller Style
    - WebFlux Demo (controller style)
    - default disable: set `spring.profiles.active=controller` to enable

### Chat Demo
`http://localhost:8080/static/websocket-demo.html`
- Reactive WebSocket Demo

### FileUpload Demo
`http://localhost:8080/static/file-upload-demo.html`
- Multiple File upload with SSE Response
