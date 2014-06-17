Scaxerciser
===
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

Play Framework, na którym działa aplikacja ściągnie automatycznie potrzebne zależności. Także inną wersję Scali (2.10.3), którą wykorzystuje Play. System został przetestowany ma podanych powyżej numerach wersji. Zaleca się, aby wersje wymaganych narzędzi były takie same dla aplikacji testującej.

System opiera się na dwóch aplikacjach. Do poprawnej komunikacji pomiędzy aplikacją zarządzającą, a testującą rozwiązania ([scaxerciser_analyze](https://github.com/dsawa/scaxerciser_analyze)) wymagana jest jedna instancja RabbitMQ.

### Uruchomienie

**Uwaga**:
Po zainstalowaniu wszystkich wymaganych narzędzi, należy uruchomić serwer RabbitMQ i MongoDB.

Na potrzeby niniejszej instrukcji, przyjmijmy, że nazwa maszyny, na której chcemy uruchomić aplikację to `inf.ug.edu.pl`.

**Kroki prowadzące do uruchomienia aplikacji**

1. ```git clone git@github.com:dsawa/scaxerciser.git```
	##### 

2. ```cd scaxerciser```
	##### 

3. Konfiguracja połączenia z MongoDB.
  ##### Pliki konfiguracyjne
  Ustawienia połączenia znajdują się w różnych plikach, określające konfiguracje dla środowisk uruchomieniowych.
  + conf/application.conf - najbardziej ogólne ustawienia. Będą stosowane we wszystkich środowiskach.
  + conf/application.dev.conf - ustawienia środowiska deweloperskiego.
  + conf/application.prod.conf - ustawienia środowiska produkcyjnego.
  + conf/application.test.conf - ustawienia środowiska testowego.
  
  ##### Opis poszczególnych ustawień:
  - `mongo.connection.host="inf.ug.edu.pl"` - nazwa maszyny, na której uruchomiony jest MongoDB.
  - `mongo.connection.port=27017` - numer portu
  - `mongo.accounts.db="scaxerciser"` - baza zawierająca kolekcję z dokumentami użytkowników.
  - `mongo.accounts.collection="users"` - nazwa kolekcji z dokumentami użytkowników
  - `mongo.groups.db="scaxerciser"` - baza zawierająca kolekcję z dokumentami grup
  - `mongo.groups.collection="groups"` - nazwa kolekcji z dokumentami grup
  - `mongo.assignments.db="scaxerciser"` - baza zawierająca kolekcję z dokumentami zadań.
  - `mongo.assignments.collection="assignments"` - nazwa kolekcji z dokumentami zadań
  - `mongo.assignments.projects.db="scaxerciser"` - baza zawierająca kolekcję z dokumentami projektów SBT zadań
  - `mongo.solutions.db="scaxerciser"` - baza zawierająca kolekcję z dokumentami rozwiązań
  - `mongo.solutions.collection="solutions"` - nazwa kolekcji z dokumentami rozwiązań
  - `mongo.solutions.projects.db="scaxerciser"` - baza zawierająca kolekcję z dokumentami projektów SBT rozwiązań

4. Konfiguracja połączenia z RabbitMQ. <br>
  Ustawienia połączenia znajdują się w tych samych plikach, o których wspomina punkt 4. <br>
  Opis poszczególnych ustawień:
    + `rabbitmq.host=inf.ug.edu.pl` - nazwa maszyny, na której uruchomiony jest RabbitMQ
    + `rabbitmq.solutions.queue=solutions_queue` - nazwa kolejki rozwiązań

5. Konfiguracja klucza dostępu dla aplikacji testującej. <br>
   Ustawienie to znajduje się w tych samych plikach, co we wcześniejszych punktach. Taki sam klucz powinien znaleźć się w ustawieniach aplikacji testującej. <br> Dzięki niemu, aplikacja testująca może wysłać powiadomienie o ocenionym rozwiązaniu.
   `scaxerciser.api.scaxerciser_analyze.token="1/fa6d425d8666b8f128befbccb8693c592hqA5c3c"`

6. Po uzupełnieniu odpowiednich pól konfiguracyjnych można uruchomić aplikację. Odpowiada za to polecenie `play run`. 
	##### 

7. Aplikacja będzie dostępna domyślnie pod adresem `inf.ug.edu.pl:9000`
	##### 

8. Do rozpoczęcia pracy należy zalogować się na konto administratora. <br>
  Domyślne dane logowania to:
    + użytkownik: admin
    + hasło: admin
