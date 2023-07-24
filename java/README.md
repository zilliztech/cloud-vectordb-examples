## Getting started
The example shows how to connect to Zilliz Cloud, create a collection with customized primary field, build
index, load collection and query a vector to get the most similar entities.
### Prerequisites

    Java 8+
    Apache Maven 3.6+

### Git clone the example code repo
    git clone https://github.com/zilliztech/cloud-vectordb-examples

### Go to milvus-java folder
    cd cloud-vectordb-examples
    cd java

### Modify uri, token or user/password
    uri = https://in01-XXXXXXXXXXXX.aws-us-west-2.vectordb.zillizcloud.com:XXXXX
    user = db_admin
    password = ********   

### Compile project
    mvn compile

### Run HelloZilliz.java
    mvn exec:java  -Dexec.mainClass="demo.HelloZilliz"

### It should print information on the console
    Connecting to Zilliz Cluster: https://in01-XXXXXXXXXXXXX.aws-us-west-2.vectordb.zillizcloud.com:XXXXX
    Success!
    Creating example collection: book
    Schema: {...}
    Success!
    Inserting 2000 entities... 
    Succeed in 0.3321 seconds!
    Flushing...
    Succeed in 0.81 seconds!
    Building AutoIndex...
    Succeed  in 18.9318 seconds!
    Loading collection...
    Succeed in 1.718 seconds!
    Searching vector:[[...][...]...]
    Result:[...]
    latency: 0.0154 seconds!
  
