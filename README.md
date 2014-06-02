## Projekt do pracy magisterskiej

Praca magisterska [master-thesis](https://github.com/dsawa/master-thesis)

Szablony projektów SBT dla Scaxercisera: [project-template](https://github.com/dsawa/project-template)

Aplikacja analizująca rozwiązania: [scaxerciser_analyze](https://github.com/dsawa/scaxerciser_analyze)

## Instalacja

#### Wymagania
+ [Oracle JDK - Java 7 lub 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ [SBT 0.13.5](http://www.scala-sbt.org/download.html)
+ [Scala 2.11.1](http://www.scala-lang.org/download/)
+ [Play Framework 2.2.3](http://www.playframework.com/download)
+ [MongoDB 2.6.1](http://www.mongodb.org/downloads)
+ [RabbitMQ Server 3.3.1](https://www.rabbitmq.com/download.html)

Play Framework, na którym działa aplikacja ściągnie automatycznie potrzebne zależności. Także inną wersje Scali (2.10.3), na której ta wersja Play'a działa. Na podanych powyżej numerach wersji kolejnych narzędzi system został przetestowany. Zaleca się zachowanie spójności między poszczególnymi elementami systemu tj. Scaxerciser_analyze i szablonem projektów.

#### Uruchomienie
1. `git clone git@github.com:dsawa/scaxerciser.git`
2. `cd scaxerciser`
3. `play`
4. Przygotować projekt pod IntelliJ IDEA lub eclipse
  + IntelliJ: `gen-idea`
  + eclipse: `eclipse`
5. Skonfigurować połączenie z MongoDB i RabbitMQ w plikach: <br>
  (w środowisku deweloperskim ustawienia domyślne sprawdzają się dobrze)
  + app/application.conf
  + app/application.dev.conf
  + app/application.prod.conf
6. `run` lub `play run` jeśli wyszliśmy wcześniej z konsoli Play Framework 
7. `http://localhost:9000/` w przeglądarce
8. Domyślny administrator: 
  + Login: admin@example.com
  + Hasło: admin
