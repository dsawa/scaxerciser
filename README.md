Scaxerciser
==
Aplikacja zarządzająca systemu weryfikacji kodu Scala. Zajmuje się wszystkimi operacjami związanymi z tworzeniem zadań, grup, czy zarządzaniem użytkownikami i ich rozwiązaniami. Udostępnia interfejs w postaci dynamicznej strony internetowej. 

**Aplikacja jest częścią pracy magisterskiej:**
+ Praca magisterska [master-thesis](https://github.com/dsawa/master-thesis)
+ Szablony projektów SBT dla Scaxercisera: [project-template](https://github.com/dsawa/project-template)
+ Aplikacja analizująca rozwiązania: [scaxerciser_analyze](https://github.com/dsawa/scaxerciser_analyze)

### Wymagania
+ [Oracle JDK - Java 7 lub 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ [SBT 0.13.5](http://www.scala-sbt.org/download.html)
+ [Scala 2.11.1](http://www.scala-lang.org/download/)
+ [Play Framework 2.2.3](http://www.playframework.com/download)
+ [MongoDB 2.6.1](http://www.mongodb.org/downloads)
+ [RabbitMQ Server 3.3.1](https://www.rabbitmq.com/download.html)

Play Framework, na którym działa aplikacja ściągnie automatycznie potrzebne zależności. Także inną wersje Scali (2.10.3), na której ta wersja Play'a działa. Na podanych powyżej numerach wersji kolejnych narzędzi system został przetestowany. Zaleca się zachowanie spójności między poszczególnymi elementami systemu tj. Scaxerciser_analyze i szablonem projektów.

System opiera się o dwie aplikacje. Do poprawnej komunikacji pomiędzy aplikacją zarządzającą, a testującą rozwiązania ([scaxerciser_analyze](https://github.com/dsawa/scaxerciser_analyze)) wymagana jest jedna instancja RabbitMQ.

### Uruchomienie

**Uwaga**:
Po zainstalowaniu wszystkich wymaganych narzędzi, należy uruchomić serwer RabbitMQ i MongoDB.

**Kroki do uruchomienia aplikacji**

1. `git clone git@github.com:dsawa/scaxerciser.git`
2. `cd scaxerciser`
3. Przygotowanie projektu pod IntelliJ IDEA lub eclipse
  + IntelliJ: `play gen-idea`
  + Eclipse: `play eclipse`
4. Konfiguracja połączenia z MongoDB. <br>
  Ustawienia połączenia znajdują się w różnych plikach, określające konfiguracje dla środowisk uruchomieniowych:
  + conf/application.conf - najbardziej ogólne ustawienia. Będą stosowane we wszystkich środowiskach.
  + conf/application.dev.conf - ustawienia środowiska deweloperskiego.
  + conf/application.prod.conf - ustawienia środowiska produkcyjnego.
  + conf/application.test.conf - ustawienia środowiska testowego. <br>
  Opis poszczególnych ustawień:
    - `mongo.connection.host="localhost"` - nazwa hosta
    - `mongo.connection.port=27017` - port
    - `mongo.accounts.db="scaxerciser"` - baza, w której jest kolekcja z dokumentami użytkowników.
    - `mongo.accounts.collection="users"` - nazwa kolekcji z dokumentami użytkowników.
    - `mongo.groups.db="scaxerciser"` - baza, w której jest kolekcja z dokumentami grup.
    - `mongo.groups.collection="groups"` - nazwa kolekcji z dokumentami grup.
    - `mongo.assignments.db="scaxerciser"` - baza, w której jest kolekcja z dokumentami zadań.
    - `mongo.assignments.collection="assignments"` - nazwa kolekcji z dokumentami zadań.
    - `mongo.assignments.projects.db="scaxerciser"` - baza, w której jest kolekcja z dokumentami projektów SBT zadań.
    - `mongo.solutions.db="scaxerciser"` - baza, w której jest kolekcja z dokumentami rozwiązań.
    - `mongo.solutions.collection="solutions"` - nazwa kolekcji z dokumentami rozwiązań.
    - `mongo.solutions.projects.db="scaxerciser_dev"` - baza, w której jest kolekcja z dokumentami projektów SBT rozwiązań.
5. Konfiguracja połączenia z RabbitMQ. <br>
  Ustawienia połączenia znajdują się w tych samych plikach, o których wspomina punkt 4.
  Opis poszczególnych ustawień:
    - `rabbitmq.host=localhost` - nazwa hosta.
    - `rabbitmq.solutions.queue=solutions_queue` - nazwa kolejki rozwiązań.
6. Po uzupełnieniu odpowiednich pól konfiguracyjnych można uruchomić aplikację. Dla środowiska deweloperskiego, w katalogu użytkownika, poleceniem: `play run` 
7. Aplikacja jest dostępna pod adresem `http://localhost:9000/` w przeglądarce.
8. Należy sie zalogować na konto administratora, aby rozpocząć pracę. Domyślny administrator:
  + Login: admin@example.com
  + Hasło: admin
