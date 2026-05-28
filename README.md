# Proyecto Teoria de Base de Datos 2

# Herramienta Administrativa de Base de Datos (Database Manager Tool)

---

# 1. Descripcion General

El proyecto desarrolla una herramienta administrativa de bases de datos utilizando Java y SQL Anywhere. La aplicación permite la conexión y autenticación de usuarios, la administración de objetos de la base de datos mediante system tables y la ejecución de sentencias SQL.

Además, incluye una interfaz gráfica para visualizar tablas, vistas, procedimientos, índices, triggers y usuarios, así como funcionalidades para generar DDL y mostrar resultados de consultas.

El sistema fue desarrollado con una arquitectura basada en separación de responsabilidades, permitiendo una administración dinámica de metadata y múltiples conexiones simultáneas.

---

# 2. Objetivo General

Desarrollar una herramienta administrativa de bases de datos utilizando Java y SQL Anywhere, que permita gestionar conexiones, administrar objetos de la base de datos y ejecutar sentencias SQL mediante el uso directo de system tables.

---

# 3. Objetivos Especificos

* Implementar un sistema de conexión y autenticación para diferentes bases de datos.

* Diseñar una interfaz gráfica que permita visualizar y administrar objetos como tablas, vistas, procedimientos, índices, triggers y usuarios.

* Ejecutar sentencias SQL y mostrar los resultados de las consultas dentro de la aplicación.

* Generar instrucciones DDL a partir de la metadata obtenida desde las system tables.

* Utilizar consultas SQL nativas sin emplear ORM ni information_schema.

* Implementar un explorador dinámico de metadata utilizando system tables.

* Soportar múltiples conexiones simultáneas dentro de la aplicación.

---

# 4. Alcance del Sistema

El sistema permite la conexión y autenticación de usuarios a bases de datos SQL Anywhere mediante una interfaz gráfica desarrollada en Java.

La herramienta administra objetos de la base de datos utilizando consultas directas a las system tables, incluyendo:

* tablas
* vistas
* procedimientos almacenados
* funciones
* triggers
* índices
* sequences
* usuarios
* tablespaces

Además, el sistema permite:

* ejecutar sentencias SQL
* visualizar resultados de consultas
* generar instrucciones DDL
* crear tablas visualmente
* crear vistas visualmente
* administrar múltiples conexiones

La aplicación proporciona un entorno similar a herramientas administrativas profesionales como pgAdmin o DBeaver.

---

# 5. Tecnologías Utilizadas

* Java
* Java Swing
* JDBC
* SQL Anywhere 17
* Apache NetBeans IDE

---

# 6. Arquitectura del Sistema

El sistema fue desarrollado utilizando una arquitectura basada en separación de responsabilidades, donde cada clase cumple una función específica dentro de la aplicación.

La estructura general del sistema se divide en las siguientes capas:

## Interfaz Gráfica (GUI)

Encargada de la interacción con el usuario mediante Java Swing.

Clases principales:

* `LoginFrame`
* `DashboardFrame`

## Capa de Conexión

Encargada de establecer la conexión JDBC con SQL Anywhere.

Clase principal:

* `DataBaseConnection`

## Capa de Ejecución SQL

Encargada de ejecutar sentencias SQL y retornar resultados dinámicamente.

Clase principal:

* `QueryExecutor`

## Capa de Metadata

Encargada de consultar las system tables y reconstruir objetos de la base de datos.

Clase principal:

* `MetaDataManager`

## Capa de Sesiones

Encargada de manejar múltiples conexiones activas dentro del sistema.

Clase principal:

* `DBConnectionSession`

---

# 7. Creacion de Clases En Java

Para la creación de este administrador se desarrolló un proyecto en Apache NetBeans utilizando Java.

El sistema está compuesto por las siguientes clases:

## ProyectoTBD2

Clase principal (`main`) encargada de iniciar la aplicación y abrir la ventana de login.

## LoginFrame

Clase encargada de manejar la autenticación y conexión a la base de datos.

Permite:

* conexión mediante archivo `.db`
* conexión mediante IP/TCP
* autenticación de usuario
* creación de múltiples conexiones

## DataBaseConnection

Clase encargada de realizar la conexión JDBC con SQL Anywhere.

Construye dinámicamente la cadena de conexión dependiendo del tipo de conexión seleccionada.

## DashboardFrame

Clase principal de la interfaz gráfica.

Implementa:

* sidebar tipo explorer
* editor SQL
* tabla dinámica de resultados
* visualización de metadata
* creación visual de tablas y vistas
* generación de DDL

## MetaDataManager

Clase encargada de consultar metadata desde las system tables de SQL Anywhere.

Implementa:

* lectura de tablas
* lectura de columnas
* lectura de views
* lectura de procedures
* lectura de triggers
* lectura de índices
* generación de DDL
* reverse engineering

## QueryExecutor

Clase encargada de ejecutar sentencias SQL dinámicamente.

Diferencia entre:

* consultas SELECT
* procedimientos CALL
* instrucciones DDL
* instrucciones DML

## DBConnectionSession

Clase utilizada para almacenar múltiples conexiones activas dentro del sistema.

Cada sesión mantiene:

* nombre de conexión
* objeto Connection

---

# 8. Conexion a Base de Datos

Para la implementación de la interfaz visual se utilizó Apache NetBeans.

La conexión entre Java y SQL Anywhere se realizó mediante JDBC (Java Database Connectivity).

Se agregó la librería:

`sajdbc4.jar`

Ubicada en:

`C:\Program Files\SQL Anywhere 17\Java\sajdbc4.jar`

La conexión se realiza mediante una cadena de conexión como la siguiente:

```java
String url = "jdbc:sqlanywhere:uid=Maria Rodriguez;pwd=********;"
        + "dbf=C:/Users/Maria Gabriela/OneDrive/Desktop/Ingenieria en Sistemas/TBD/22441044_ProyectoTBD.db";
```

Esto permite que la aplicación Java pueda ejecutar consultas SQL directamente en la base de datos.

La conexión utilizada fue mediante un archivo `.db` utilizando `dbf` (database file), ya que la base de datos era local.

El sistema también soporta conexión mediante TCP/IP utilizando:

* host
* puerto
* engine

---

# 9. Uso de System Tables

El sistema utiliza consultas directas a las system tables de SQL Anywhere para obtener metadata sobre los objetos de la base de datos.

Entre las principales tablas utilizadas se encuentran:

| System Table   | Función                            |
| -------------- | ---------------------------------- |
| `SYSTABLE`     | Obtiene tablas y vistas            |
| `SYSCOLUMN`    | Obtiene columnas y tipos de datos  |
| `SYSPROCEDURE` | Obtiene procedimientos y funciones |
| `SYSTRIGGER`   | Obtiene triggers                   |
| `SYSINDEX`     | Obtiene índices                    |
| `SYSUSER`      | Obtiene usuarios                   |
| `SYSSEQUENCE`  | Obtiene sequences                  |
| `SYSDOMAIN`    | Obtiene tipos de datos internos    |
| `SYSDBSPACE`   | Obtiene tablespaces/dbspaces       |

El uso de estas tablas permitió implementar un explorador dinámico de metadata y generación automática de DDL.

---

# 10. Funcionalidades Implementadas

El sistema implementa las siguientes funcionalidades:

* Conexión a bases de datos SQL Anywhere
* Soporte para múltiples conexiones simultáneas
* Explorador dinámico de metadata
* Visualización de tablas y columnas
* Visualización de vistas
* Visualización de procedimientos y funciones
* Visualización de triggers
* Visualización de índices
* Visualización de usuarios
* Visualización de sequences
* Visualización de tablespaces
* Ejecución de sentencias SQL
* Ejecución de consultas SELECT
* Ejecución de DDL y DML
* Visualización dinámica de resultados
* Generación automática de DDL
* Creación visual de tablas
* Creación visual de vistas
* Recarga automática del árbol de metadata

---

# 11. Manejo de Metadata

La metadata es obtenida directamente desde el catálogo interno del DBMS mediante consultas SQL.

El sistema utiliza relaciones entre system tables para identificar:

* propietarios de objetos
* tipos de datos
* llaves primarias
* columnas
* índices
* procedimientos almacenados

Esto permitió construir un explorador dinámico similar a herramientas administrativas profesionales.

---

# 12. Generación de DDL

El sistema implementa generación dinámica de instrucciones DDL utilizando metadata obtenida desde las system tables.

Entre los objetos soportados se encuentran:

* CREATE TABLE
* CREATE VIEW
* CREATE PROCEDURE
* CREATE FUNCTION
* CREATE TRIGGER
* CREATE INDEX

La generación de DDL se realiza mediante reverse engineering de la metadata almacenada por SQL Anywhere.

---

# 13. Interfaz Gráfica

La interfaz gráfica fue desarrollada utilizando Java Swing.

La aplicación incluye:

* Sidebar tipo explorer
* Editor SQL
* Tabla dinámica de resultados
* Ventanas de creación visual de objetos
* Menús contextuales
* Visualización de DDL

La estructura visual se desarrolló utilizando:

* JFrame
* JPanel
* JTable
* JTree
* JSplitPane
* JScrollPane
* JTextArea
* JOptionPane

---

# 14. Manejo de Consultas SQL

La clase `QueryExecutor` se encarga de ejecutar sentencias SQL dinámicamente.

El sistema diferencia automáticamente:

* consultas SELECT
* procedimientos CALL
* instrucciones DDL
* instrucciones DML

Para las consultas SELECT se utiliza:

```java
executeQuery()
```

Mientras que para instrucciones DDL y DML se utiliza:

```java
execute()
```

Los resultados son cargados dinámicamente utilizando:

```java
ResultSetMetaData
```

Esto permite construir tablas de resultados sin definir columnas manualmente.

---

# 15. Conclusiones

El proyecto permitió aplicar conceptos avanzados de administración de bases de datos, JDBC y metadata utilizando SQL Anywhere y Java.

Se logró desarrollar una herramienta administrativa funcional capaz de:

* conectarse a múltiples bases de datos
* explorar metadata dinámicamente
* ejecutar consultas SQL
* generar DDL automáticamente
* administrar objetos de la base de datos

Además, el proyecto permitió comprender el funcionamiento interno de los catálogos del sistema y el manejo de metadata dentro de un DBMS.




