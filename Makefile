CURRENT_VERSION=1.0-SNAPSHOT

.PHONY: all run clean

all:
	./gradlew jar

run: all
	kotlin -J-ea -cp build/libs/Chess-$(CURRENT_VERSION).jar app.Main

clean: 
	./gradlew clean
