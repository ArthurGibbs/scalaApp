


INSTALL

choco install sbt


#ONE OFF
sbt new playframework/play-scala-seed.g8




sbt -mem 4092 -jvm-debug 999


run



### DB SETUP


docker pull postgres:14.5

docker volume create duenna_postgres_data

docker run --name duenna_postgres_14.5 -e POSTGRES_PASSWORD=password -d -p 5432:5432 -v duenna_postgres_data:/var/lib/postgresql/data postgres:14.5



```bash
    > psql -h localhost -U postgres
```

then:

```sql
    create user "app" with password 'password';
    create database "app" with owner "app";
    alter user app with SUPERUSER;

    quit
```




