## Getting started

### Prerequisites

    Java 8 or higher
    Apache Maven 

### Git clone the example code repo
    git clone https://github.com/zilliztech/cloud-vectordb-examples

### Go to milvis-java folder
    cd cloud-vectordb-examples
    cd milvus-java

### Compile project
    mvn compile

### Modify endpoint and user password in the ConnectParam
     ConnectParam.newBuilder()
                        .withHost("in01-XXXXXXXXXXXXX.ap-southeast-1-aws.vdc-test.zilliz.com")
                        .withPort(19530)
                        .withAuthorization("root","******")
                        .withSecure(true)
                        .build());

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