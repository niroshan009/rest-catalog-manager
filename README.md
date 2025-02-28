### Pre-requisites
We need to have a postgres database running. We can use the following command to run a postgres database in a docker container.

``` shell
docker run -d --name mypostgres -p 5432:5432 -e POSTGRES_PASSWORD=yourpassword postgres
```

### Running the application
We can run the application using the following command.

```shell
java -jar target/rest-catalog-manager-0.0.1-SNAPSHOT.jar --workflow=UPDATE --tag=version1
```
In above snippet, we are running the application with `UPDATE` workflow and `version1` tag.

If we want to roll back the changes, we can run the application with `ROLLBACK` workflow and `version1` tag.

```shell
java -jar target/rest-catalog-manager-0.0.1-SNAPSHOT.jar --workflow=ROLLBACK --tag=version1
```