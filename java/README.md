## Getting started

### Prerequisites

    -   Java 8 or higher
    -   Apache Maven or Gradle/Grails

### Install Java SDK

You can use **Apache Maven** or **Gradle**/**Grails** to download the SDK.

- Apache Maven

    ```xml
     <dependency>
         <groupId>io.milvus</groupId>
         <artifactId>milvus-sdk-java</artifactId>
         <version>2.0.4</version>
     </dependency>
    ```

- Gradle/Grails

     ```gradle
     compile 'io.milvus:milvus-sdk-java:2.0.4'
     ```

### Create Project
    
- Copy HelloMilvus.java into your project.
- Login Vector Database Cloud and ensure your instance is running
- Copy your instance's host and Authorization, replace connect params