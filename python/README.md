## Getting started

### Prerequisites
    Install Python 3.7+
    pip3


### Git clone the example code repo
    git clone https://github.com/zilliztech/cloud-vectordb-examples.git

### Install pymilvus
    pip3 install pymilvus

### Go to python folder
    cd cloud-vectordb-examples
    cd python

### Modify uri, token in the configuration file.(config.ini)
    uri = https://in01-XXXXXXXXXXXX.aws-us-west-2.vectordb.zillizcloud.com:XXXXX
    token = replace-this-with-your-token
*The token value can be either the API key or username:password.*     

### Run hello_milvus.py to run
    python3 hello_zilliz_vectordb.py

### It should print information on the console
    Connecting to DB: https://in01-xxxxxxxxxxxxx.aws-us-west-2.vectordb.zillizcloud.com:XXXXX
    Success!
    Creating example collection: book
    Schema: {...}
    Success!
    Inserting 100000 entities... 
    Succeed in 6.0288 seconds!
    Building AutoIndex...
    Succeed in 18.9118 seconds!
    Loading collection...
    Succeed in 2.5229 seconds!
    Searching vector:[[...][...]...]
    search 0 latency: 0.0057 seconds!
    Searching vector:[[...][...]...]
    search 1 latency: 0.0049 seconds!
    Searching vector:[[...][...]...]
    search 2 latency: 0.0051 seconds!
    ...
    ...
    