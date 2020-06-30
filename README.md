# kalah project

Hi all, 

This should show some of my coding capabilities.

For the core I've spent about 5 hours.
Test coverage isn't 100% but decent.

I'll spend some more time adding some goodies.

It uses Maven. I'm more into gradle recently but it was a good opportunity to dust off my maven skills.


Very basic:
Testing:

```bash
mvn test
```

Run in dev mode using the quarkus hot-swapping runtime:
```bash
mvn quarkus:dev
```
(stop using ctrl-c, server runs on 8080)

To run stand-alone
```bash
cd target
java -jar kalah-1.1-SNAPSHOT-runner.jar
```

### Deploy to Google Cloud Run (might lead to auth issues):
mvn install -X -Dquarkus.native.container-build=true \
    -Dquarkus.container-image.push=true \
    -Dquarkus.container-image.name=kalah \
    -Dquarkus.container-image.group=eu.gcr.io/kalah-281706


Quarkus and GraalVM also allow for building native apps, unfortunately the google-firestore client does not support that at this moment, it is a pity, as the native version starts much
faster (which is nice for a scale-to-zero situation like I've set up here).
Also, the native version easily fits in the smallest container instance (128Mb of memory), while the JVM based version needs 256Mb.

Anyway, the JVM version runs here:

https://kalah-game-s5kyy5jiha-ez.a.run.app/games

If there is an issue building you can also build the docker image manually:
```bash
mvn package
docker build -f src/main/docker/Dockerfile.jvm -t flyaruu/kalah:1.2.0-SNAPSHOT .
```
And run it:
```bash
docker run -p 8080:8080 flyaruu/kalah:1.2.0-SNAPSHOT
```

## "Roadmap"
Things that could be better (but I think I've spent enough time on this assignment for now)
 - Add swagger API doc
 - Add full CI/CD pipeline
 - Build JavaDoc
 - Add linter to build like CheckCode
 

# Standard Quarkus build:

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `kalah-1.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/kalah-1.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/kalah-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.