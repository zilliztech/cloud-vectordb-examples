## Getting started

### Prerequisites
    Install Python 3.7+
    pip


### Git clone the example code repo
    git clone https://github.com/zilliztech/cloud-vectordb-examples.git

### Install pymilvus（2.1.0.dev98 or latest）
    pip install -i https://test.pypi.org/simple/ pymilvus==2.1.0.dev98

### Go to python folder
    cd cloud-vectordb-examples
    cd python

### Modify uri, username and user password in the configuration file.(config.ini)
    uri = in01-xxxxxxxxxxxxx.aws-ap-southeast-1.vectordb-sit.zillizcloud.com
    port = 19530
    user = your-username
    password = your-password

### Run hello_milvus.py to run
    Python hello_zilliz_vectordb.py

### It should print information on the console
    totally insert 100000 entities cost xx seconds
    collection book entities: 100000
    collection book build index in xx seconds
    collection book load in xx seconds
    search 0 latency: 0.xxx seconds
    search 1 latency: 0.xxx seconds
    search 2 latency: 0.xxx seconds
    ...
    ...
    