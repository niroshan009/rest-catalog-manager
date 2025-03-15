## About this project
This project can be utilized to DDL automation for the environments. Imagine a scenario where you want to create your iceberg tables in the multiple environments.\
You can create and delete tables by defining your actions in the ```application.yaml``` file. 

## DDL Management
### Create Table
Create table by defining as below in the ```application.yaml``` file. 

```yaml
catalog:
  changes:
    - name: "Create test table"
      id: 1
      description: "create test 1 table"
      author: "test"
      table: "test_table1"
      namespace: "nyc"
      rollback_struct: "./change_jsons/create_test_table.json"
      action: "create"
      struct: "./change_jsons/create_test_table.json"
```

This change log will create a table in the postgres database table to track the history of the tables that was created.
Structure of the table should be defined in a json that will be used for ```POST``` call tabulario rest api to create the table in iceberg.

### Drop Table
Include below in the ```application.yaml``` file.
```yaml
catalog:
  changes:
    - name: "Create test table"
      id: 1
      description: "create test 1 table"
      author: "test"
      table: "test_table1"
      namespace: "nyc"
      rollback_struct: "./change_jsons/create_test_table.json"
      action: "drop"
      struct: "./change_jsons/create_test_table.json"
```
This changelog will drop an existing table. Defining of the ```struct``` field is necessary to handle any error while executing changeset this will help to create the original table again in case of changeset failure.

## Actions
Current implementation supports ```UPDATE``` and ```ROLLBACK``` where 
* UPDATE: will execute changesets in a manner that will insert new rows to the change ```change_log``` table
* ROLLBACK: will execute changesets in a manner that will delete the executed changeset earlier. This will Roll back from LIFO approach. Last executed will be deleted first and revert the changes affected to iceberg

## Change management and Exception handling
This will check the last executed changes and just run new changes without executing previous changes\
In case of exception to one changeset will roll back current executing changeset.


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
