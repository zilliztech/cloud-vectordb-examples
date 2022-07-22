## Getting started

### Prerequisites
    Install Python 3.7+
    pip


### Git clone the example code repo
    git clone https://github.com/zilliztech/cloud-vectordb-examples.git

### Install pymilvus
    pip install pymilvus==2.1.0

### Go to python folder
    cd cloud-vectordb-examples
    cd python

### Modify uri, username and user password in the configuration file.(config.ini)
    uri = in01-xxxxxxxxxxxxx.aws-us-west-2.vectordb.zillizcloud.com
    port = 19530
    user = your-username
    password = your-password

### Run hello_milvus.py to run
    Python hello_zilliz_vectordb.py

### It should print information on the console
    start to connect to in01-xxxxxxxxxxxxx.aws-us-west-2.vectordb.zillizcloud.com
    create collection book successfully
    totally insert 100000 entities cost 6.0288 seconds
    collection book entities: 100000
    collection book load in 2.5229 seconds
    search 0 latency: 0.0057 seconds
    search 1 latency: 0.0049 seconds
    search 2 latency: 0.0051 seconds
    ...
    ...
    