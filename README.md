# Load manager

[![Build Status][travis-img]][travis-url]

The service provides an HTTP API for organizing microgrid management platforms
into groups, and obtaining their aggregated load.

## Configuration

The service is configured using the following environment variables:

| Variable     | Description                                             | Required |
|--------------|---------------------------------------------------------|----------|
| APP_SECRET   | Secret key used by Play framework, defaults to 'secret' | No       |
| DB_NAME      | Name of the database the service will use               | Yes      |
| DB_HOST      | Database instance host                                  | Yes      |
| DB_PORT      | Database instance port                                  | Yes      |
| DB_USER      | Database user used by the service                       | Yes      |
| DB_PASS      | Database password used by the service                   | Yes      |

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
      DB_NAME: load-manager
      DB_HOST: postgres
      DB_PORT: 5432
      DB_USER: mainflux
      DB_PASS: mainflux

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
[travis-img]: https://travis-ci.org/MainfluxLabs/loadmanager.svg?branch=dev
[travis-url]: https://travis-ci.org/MainfluxLabs/loadmanager
