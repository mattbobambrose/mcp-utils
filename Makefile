default: versioncheck

clean:
	./gradlew clean

build: clean
	./gradlew build

jar: build
	./gradlew SSEServer StdioServer

versioncheck:
	./gradlew dependencyUpdates

runsse:
	java -jar /Users/mambrose/git/LLM-internals-exercise/build/libs/SSEServer.jar