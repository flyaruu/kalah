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

### Deploy to Google Cloud Run:
mvn install -X -Dquarkus.native.container-build=true \
    -Dquarkus.container-image.push=true \
    -Dquarkus.container-image.name=kalah \
    -Dquarkus.container-image.group=eu.gcr.io/kalah-281706


Quarkus and GraalVM also allow for building native apps.


Some Quarkus things:

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