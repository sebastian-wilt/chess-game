CURRENT_VERSION=1.0-SNAPSHOT

.PHONY: all
all:
	./gradlew jar

.PHONY: run
run: all
	kotlin -cp build/libs/Chess-$(CURRENT_VERSION).jar app.Main

.PHONY: clean 
clean: 
	./gradlew clean
