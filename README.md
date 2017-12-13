# Load manager

[![Build Status][travis-img]][travis-url]

The service provides an HTTP API for organizing microgrid management platforms
into groups, and obtaining their aggregated load.

## Configuration

The service is configured using the following environment variables:

| Variable     | Description                                             | Required |
|--------------|---------------------------------------------------------|----------|
| APP_SECRET   | Secret key used by Play framework, defaults to 'secret' | No       |
| DATABASE_URL | JDBC connection string                                  | Yes      |

## Usage

In order to run the service locally, the following dependencies must be installed:
- [Docker][docker-url]
- [Compose][compose-url]

Once all the dependencies are installed, save the following Compose file as
`docker-compose.yml`:

```yaml
version: '3'

services:
  lm:
    image: mainflux/load-manager:latest
    ports:
      - "9000:9000"
    environment:
      DATABASE_URL: postgres://mainflux:mainflux@postgres/load-manager

  postgres:
    image: postgres:9.6-alpine
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: load-manager
      POSTGRES_USER: mainflux
      POSTGRES_PASSWORD: mainflux
```

Use the system's terminal window to navigate to the directory where the file has
been saved, and execute the following command:

```bash
docker-compose up -d
```

After the composition is started, visit the `http://localhost:9000/docs` to access
the interactive API documentation.

[compose-url]: https://docs.docker.com/compose/overview/
[docker-url]: https://docker.com
[travis-img]: https://travis-ci.org/MainfluxLabs/loadmanager.svg?branch=master
[travis-url]: https://travis-ci.org/MainfluxLabs/loadmanager
