default: versioncheck

clean:
	./gradlew clean

build: clean
	./gradlew build -xtest

jar: build
	./gradlew SSEServer StdioServer

runsse:
	java -jar /Users/mambrose/git/LLM-internals-exercise/build/libs/SSEServer.jar

versioncheck:
	./gradlew dependencyUpdates

upgrade-wrapper:
	./gradlew wrapper --gradle-version=9.3.1 --distribution-type=bin
