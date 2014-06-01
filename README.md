## Projekt do pracy magisterskiej

Praca magisterska [master-thesis](https://github.com/dsawa/master-thesis)

Szablony projektów SBT dla Scaxercisera: [project-template](https://github.com/dsawa/project-template)

## Instalacja

#### Wymagania
+ [SBT 0.13.1](http://www.scala-sbt.org/download.html)
+ [Scala 2.10+](http://www.scala-lang.org/download/)
+ [Play Framework 2.2.1](http://www.playframework.com/download)
+ [MongoDB 2.4.10+](http://www.mongodb.org/downloads)
+ [RabbitMQ Server 3.3.1](https://www.rabbitmq.com/download.html)


#### Uruchomienie
1. `git clone git@github.com:dsawa/scaxerciser.git`
2. `cd scaxerciser`
3. Przygotować projekt pod IntelliJ IDEA lub eclipse
  + IntelliJ: `play gen-idea`
  + eclipse: `play eclipse`
4. Skonfigurować połączenie z MongoDB i RabbitMQ w plikach:
  + app/application.conf
  + app/application.dev.conf
  + app/application.prod.conf
5. `play run`
6. `http://localhost:9000/` w przeglądarce
7. Domyślny administrator: 
  + Login: admin@example.com
  + Hasło: admin
