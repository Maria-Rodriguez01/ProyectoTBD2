# Proyecto Teoria de Base de Datos 2
# Herramienta Administrativa de Base de Datos(Database Manager Tool)

---
## 1.Descripcion General

El proyecto desarrolla una herramienta administrativa de bases de datos utilizando Java y SQL Anywhere. La aplicación permite la conexión y autenticación de usuarios, la administración de objetos de la base de datos mediante system tables y la ejecución de sentencias SQL. Además, incluye una interfaz gráfica para visualizar tablas, vistas, procedimientos, índices, triggers y usuarios, así como funcionalidades para generar DDL y mostrar resultados de consultas.

---
## 2.Objetivo General

Desarrollar una herramienta administrativa de bases de datos utilizando Java y SQL Anywhere, que permita gestionar conexiones, administrar objetos de la base de datos y ejecutar sentencias SQL mediante el uso directo de system tables.

---
## 3.Objetivos Especificos

- Implementar un sistema de conexión y autenticación para diferentes bases de datos.

- Diseñar una interfaz gráfica que permita visualizar y administrar objetos como tablas, vistas, procedimientos, índices, triggers y usuarios.

- Ejecutar sentencias SQL y mostrar los resultados de las consultas dentro de la aplicación.

- Generar instrucciones DDL a partir de la metadata obtenida desde las system tables.

- Utilizar consultas SQL nativas sin emplear ORM ni information_schema.
  
---

## 4.Alcance del Sistema

El sistema permite la conexión y autenticación de usuarios a bases de datos SQL Anywhere mediante una interfaz gráfica desarrollada en Java. La herramienta administra objetos de la base de datos utilizando consultas directas a las system tables, incluyendo tablas, vistas, procedimientos almacenados, triggers, índices y usuarios.

Además, el sistema permite ejecutar sentencias SQL, visualizar resultados de consultas y generar instrucciones DDL de los objetos administrados. La aplicación soporta múltiples conexiones y proporciona un entorno similar a una herramienta administrativa de bases de datos.

---

## 5.Tecnologías Utilizadas

- Java (NetBeans IDE)
- Java Swing
- JDBC

---

## 6. Creacion de Clases En Java

Para la creacion de este administrador, se necesita crear un nuevo proyecto de netbeans, donde se hara acabo el mismo. En este caso se creo 6 clases, 
las cuales cada una tiene los siguientes propositos:

- ProyectoTBD2: la clase main y encargada de llamar a el login para empezar el programa
- LoginFrame: clase en la que se maneja la informacion ingresada para ingresar a la base de datos basado en sql anywhere y los atributos que tiene esa base de datos, ya sea un archivo local o una base de datos que se pueda conectar por medio del IP
- DataBaseConnection: esta clase es la que se encarga de conectar a la base de datos que se ingrese en el login
- DashBoardFrame: clase que incluye todo el interfaz visual del administrador y se encarga que aparezcan los objetos de la base de datos de la base de datos en uso
- MetaDataManager: clase que maneja todos los sql que hacen los system tables para leer toda la metadata dentro la base de datos que se ha ingresado
- QueryExecutor: clase encargada de manejar todos los sql que se quieran ejecutar

## 7. Coneccion a Base de Datos


