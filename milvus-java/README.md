## Getting started

### Prerequisites

    Java 8+
    Apache Maven 3.6+

### Git clone the example code repo
    git clone https://github.com/zilliztech/cloud-vectordb-examples

### Go to milvus-java folder
    cd cloud-vectordb-examples
    cd milvus-java

### Modify endpoint,user name and user password in configuration file.(resources/RunSettings.properties)
    endpoint = in01-XXXXXXXXXXXX.ap-southeast-1-aws.vdc-test.zilliz.com
    port = 19530
    username = XXXXXXX
    password = ******

### Compile project
    mvn compile

### Run HelloMilvus.java
    mvn exec:java  -Dexec.mainClass="demo.HelloMilvus"

### It should print information on the console
    Create collection:Success
    Create partition: Success
    Collection info:Collection Description...
    Insert success rows: 2000
    Insert success rows: 2000
    Load collection:Success
    Search book_id result: [1886, 1652]
    Search word_count result: [11886, 11652]
    ...
    ...