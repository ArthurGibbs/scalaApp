##Setup for code

INSTALL
choco install sbt

#ONE OFF (only required for project creation)
sbt new playframework/play-scala-seed.g8

# run sbt with 4 gigs of memory and debug port 999
sbt -mem 4092 -jvm-debug 999

run

### Database SETUP

docker pull postgres:14.5
docker volume create cask_postgres_data
docker run --name cask_postgres_14.5 -e POSTGRES_PASSWORD=password -d -p 5432:5432 -v cask_postgres_data:/var/lib/postgresql/data postgres:14.5

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



to send emails we require oath2
requires project on https://console.cloud.google.com/apis/credentials
with OAuth 2.0 Client IDs
Authorised redirect URIs
can be any https link we trust,, have not worked out how to ommit
using scripts/oath2.py we can supply clientid and secret to --generate_oauth2_token //todo word better
this will make a token request link, we can follow that to the authorization page, which will redirect to the redirect link on success.. 
open devtools with preserve log on to catch the code, this can then be used in the oath2.py script to get the refresh token, needed in local.conf





