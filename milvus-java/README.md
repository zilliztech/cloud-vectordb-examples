## Getting started

### Prerequisites

    Java 8+
    Apache Maven 3.6+

### Git clone the example code repo
    git clone https://github.com/zilliztech/cloud-vectordb-examples

### Go to milvus-java folder
    cd cloud-vectordb-examples
    cd milvus-java

### Modify uri, token in configuration file.(resources/RunSettings.properties)
    uri = https://in01-XXXXXXXXXXXX.aws-us-west-2.vectordb.zillizcloud.com:XXXXX
    token = XXXX946dc4aa48632fd5f97a0370c8db10c3525254fcde78d67584198cb992528aa90a8533a6193a8a1a90a3b003400082f9XXXX
*token accepts APIKey or the format of username:password*    

### Compile project
    mvn compile

### Run HelloMilvus.java
    mvn exec:java  -Dexec.mainClass="demo.HelloZillizVectorDB"

### It should print information on the console
    Connecting to DB: https://in01-XXXXXXXXXXXXX.aws-us-west-2.vectordb.zillizcloud.com:XXXXX
    Success!
    Creating example collection: book
    Schema: {...}
    Success!
    Inserting 100000 entities... 
    Succeed in 9.3321 seconds!
    Building AutoIndex...
    Succeed  in 18.9318 seconds!
    Loading collection...
    Succeed in 1.718 seconds!
    Searching vector:[[...][...]...]
    search 0 latency: 0.0154 seconds!
    Searching vector:[[...][...]...]
    search 1 latency: 0.0147 seconds!
    Searching vector:[[...][...]...]
    search 2 latency: 0.0151 seconds!
    ...
    ...
  