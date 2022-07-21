## Getting started

### Prerequisites

    Java 8+
    Apache Maven 3.6+

### Git clone the example code repo
    git clone https://github.com/zilliztech/cloud-vectordb-examples

### Go to milvus-java folder
    cd cloud-vectordb-examples
    cd milvus-java

### Modify uri, user name and user password in configuration file.(resources/RunSettings.properties)
    uri = in01-XXXXXXXXXXXX.aws-us-west-2.vectordb.zillizcloud.com
    port = 19530
    user = your-username
    password = your-password

### Compile project
    mvn compile

### Run HelloMilvus.java
    mvn exec:java  -Dexec.mainClass="demo.HelloZillizVectorDB"

### It should print information on the console
    totally insert 100000 entities cost 87 seconds
    collection book load in 1.718 seconds
    search 0 latency: 0.0184 seconds
    search 1 latency: 0.0187 seconds
    search 2 latency: 0.0171 seconds
    ...
    ...